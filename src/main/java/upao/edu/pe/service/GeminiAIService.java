package upao.edu.pe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import upao.edu.pe.model.MensajeSunat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class GeminiAIService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.enabled:true}")
    private boolean geminiEnabled;

    @Value("${gemini.rate.limit.per.minute:12}")
    private int rateLimitPerMinute;

    @Value("${gemini.circuit.breaker.failure.threshold:5}")
    private int circuitBreakerFailureThreshold;

    @Value("${gemini.circuit.breaker.reset.timeout:300}")
    private int circuitBreakerResetTimeoutSeconds;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Rate limiting y circuit breaker
    private final ConcurrentLinkedQueue<LocalDateTime> requestHistory = new ConcurrentLinkedQueue<>();
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile LocalDateTime circuitBreakerOpenTime = null;
    private final AtomicInteger currentMinuteRequests = new AtomicInteger(0);
    private volatile LocalDateTime lastResetTime = LocalDateTime.now();    public GeminiAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Analiza un correo usando Gemini AI para determinar su clasificación y etiqueta
     */
    public EmailAnalysisResult analyzeEmail(MensajeSunat mensaje) {
        if (!geminiEnabled) {
            log.warn("Gemini AI está deshabilitado. Usando clasificación por defecto.");
            return getDefaultClassification();
        }

        // Verificar circuit breaker
        if (isCircuitBreakerOpen()) {
            log.warn("🔴 Circuit breaker abierto. Gemini AI temporalmente deshabilitado. Usando clasificación por defecto.");
            return getDefaultClassification();
        }

        // Verificar si debe procesar este correo (priorización)
        if (!shouldProcessEmail(mensaje)) {
            log.info("⏭️ Correo omitido por priorización para conservar cuota de Gemini");
            return getDefaultClassification();
        }

        // Verificar rate limit
        if (!canMakeRequest()) {
            log.warn("⏳ Rate limit alcanzado. Usando clasificación por defecto para conservar cuota.");
            return getDefaultClassification();
        }

        try {
            String prompt = buildImprovedPrompt(mensaje);
            String geminiResponse = callGeminiAPIWithRetry(prompt);
            EmailAnalysisResult result = parseGeminiResponse(geminiResponse);            
            // Marcar como éxito
            onSuccessfulRequest();
            
            // ✅ CONFIAMOS EN GEMINI: No sobreescribimos su clasificación inteligente
            log.info("🧠 Gemini clasificó inteligentemente: {}", result.getClasificacion());
            
            return result;
            
        } catch (Exception e) {
            // Manejar errores específicos
            onFailedRequest(e);
            log.error("Error al analizar correo con Gemini AI: {}", e.getMessage());
            return getDefaultClassification();
        }
    }

    /**
     * Verifica si el circuit breaker está abierto
     */
    private boolean isCircuitBreakerOpen() {
        if (circuitBreakerOpenTime == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long secondsSinceOpen = ChronoUnit.SECONDS.between(circuitBreakerOpenTime, now);
        
        if (secondsSinceOpen >= circuitBreakerResetTimeoutSeconds) {
            log.info("🟡 Intentando cerrar circuit breaker después de {} segundos", secondsSinceOpen);
            circuitBreakerOpenTime = null;
            consecutiveFailures.set(0);
            return false;
        }
        
        return true;
    }

    /**
     * Determina si debe procesar este correo específico (priorización)
     */
    private boolean shouldProcessEmail(MensajeSunat mensaje) {
        // Si ya hay muchos fallos consecutivos, solo procesar correos prioritarios
        if (consecutiveFailures.get() >= 3) {
            return isPriorityEmail(mensaje);
        }
        
        // Si el rate limit está al 80%, solo procesar correos prioritarios
        LocalDateTime now = LocalDateTime.now();
        if (ChronoUnit.MINUTES.between(lastResetTime, now) == 0) {
            if (currentMinuteRequests.get() >= (rateLimitPerMinute * 0.8)) {
                return isPriorityEmail(mensaje);
            }
        }
        
        return true;
    }

    /**
     * Determina si un correo es prioritario
     */
    private boolean isPriorityEmail(MensajeSunat mensaje) {
        if (mensaje.getVcAsunto() == null) {
            return false;
        }
        
        String asunto = mensaje.getVcAsunto().toLowerCase();
        
        // Correos de alta prioridad
        return asunto.contains("resolución") ||
               asunto.contains("cobranza") ||
               asunto.contains("embargo") ||
               asunto.contains("multa") ||
               asunto.contains("fiscalización") ||
               asunto.contains("urgente") ||
               (mensaje.getNuUrgente() != null && mensaje.getNuUrgente() == 1);
    }

    /**
     * Verifica si se puede hacer una nueva request (rate limiting)
     */
    private boolean canMakeRequest() {
        LocalDateTime now = LocalDateTime.now();
        
        // Resetear contador si ha pasado un minuto
        if (ChronoUnit.MINUTES.between(lastResetTime, now) >= 1) {
            currentMinuteRequests.set(0);
            lastResetTime = now;
        }
        
        // Verificar si se puede hacer la request
        if (currentMinuteRequests.get() >= rateLimitPerMinute) {
            return false;
        }
        
        // Limpiar historial viejo (más de 1 minuto)
        requestHistory.removeIf(timestamp -> 
            ChronoUnit.MINUTES.between(timestamp, now) > 1);
        
        return requestHistory.size() < rateLimitPerMinute;
    }

    /**
     * Registra una request exitosa
     */
    private void onSuccessfulRequest() {
        requestHistory.offer(LocalDateTime.now());
        currentMinuteRequests.incrementAndGet();
        consecutiveFailures.set(0);
        log.debug("✅ Request exitosa a Gemini. Total en minuto actual: {}", currentMinuteRequests.get());
    }

    /**
     * Maneja una request fallida
     */
    private void onFailedRequest(Exception e) {
        int failures = consecutiveFailures.incrementAndGet();
        
        // Si es error 429 (quota exceeded), abrir circuit breaker inmediatamente
        if (e instanceof HttpClientErrorException) {
            HttpClientErrorException httpError = (HttpClientErrorException) e;
            if (httpError.getStatusCode().value() == 429) {
                log.error("🔴 Error 429 - Cuota de Gemini agotada. Abriendo circuit breaker por {} segundos", 
                         circuitBreakerResetTimeoutSeconds);
                circuitBreakerOpenTime = LocalDateTime.now();
                return;
            }
        }
        
        // Abrir circuit breaker si hay muchos fallos consecutivos
        if (failures >= circuitBreakerFailureThreshold) {
            log.error("🔴 Demasiados fallos consecutivos ({}). Abriendo circuit breaker por {} segundos", 
                     failures, circuitBreakerResetTimeoutSeconds);
            circuitBreakerOpenTime = LocalDateTime.now();
        }
    }

    /**
     * Construye el prompt para enviar a Gemini AI
     */
    private String buildAnalysisPrompt(MensajeSunat mensaje) {
        StringBuilder prompt = new StringBuilder();        prompt.append("🔍 ANÁLISIS TRIBUTARIO SUNAT - CLASIFICACIÓN ESTRICTA\n\n");
        
        prompt.append("📧 ASUNTO: ").append(mensaje.getVcAsunto()).append("\n\n");
        
        prompt.append("🎯 REGLAS OBLIGATORIAS:\n");
        prompt.append("🔴 MUY IMPORTANTE = Orden de Pago, Cobranza, Embargo, Fiscalización → Etiqueta 11 o 14\n");
        prompt.append("🟢 INFORMATIVO = Formulario, Constancia, Pago de tributo, Planilla, PDT → Etiqueta 10\n");
        prompt.append("⚪ RECURRENTE = Notificación genérica, Avisos → Etiqueta 16\n\n");
        
        prompt.append("📋 EJEMPLOS:\n");
        prompt.append("'Orden de Pago No: 123' → MUY IMPORTANTE (11)\n");
        prompt.append("'Formulario 0621 - PDT IGV' → INFORMATIVO (10)\n");
        prompt.append("'Constancia de presentación' → INFORMATIVO (10)\n\n");
        
        if (mensaje.getNuUrgente() != null && mensaje.getNuUrgente() == 1) {
            prompt.append("MARCADO COMO: URGENTE\n");
        }
          prompt.append("\nInstrucciones:\n");
        prompt.append("1. Primero determina la ETIQUETA más apropiada basándote en el contenido del correo:\n");
        prompt.append("   - NO ETIQUETADOS (00): Sin clasificar o información insuficiente\n");
        prompt.append("   - VALORES (10): Pagos, valores, montos, facturación\n");
        prompt.append("   - RESOLUCIONES DE COBRANZAS (11): Cobranzas, embargos, retenciones, deudas, resoluciones coactivas, ejecución coactiva\n");
        prompt.append("   - RESOLUCIONES NO CONTENCIOSAS (13): Resoluciones administrativas, procedimientos\n");
        prompt.append("   - RESOLUCIONES DE FISCALIZACION (14): Auditorías, fiscalizaciones, inspecciones\n");
        prompt.append("   - RESOLUCIONES ANTERIORES (15): Resoluciones históricas, antecedentes\n");
        prompt.append("   - AVISOS (16): Notificaciones generales, recordatorios, información\n\n");        prompt.append("2. Luego determina la CLASIFICACIÓN automáticamente según estas reglas:\n");
        prompt.append("   - MUY IMPORTANTE: Solo para RESOLUCIONES DE COBRANZAS (11) y RESOLUCIONES DE FISCALIZACION (14)\n");
        prompt.append("   - IMPORTANTE: Solo para RESOLUCIONES NO CONTENCIOSAS (13) y RESOLUCIONES ANTERIORES (15)\n");
        prompt.append("   - RECURRENTE: Solo para AVISOS (16), VALORES (10) y NO ETIQUETADOS (00)\n\n");        prompt.append("3. Ejemplos de análisis:\n");
        prompt.append("   - \"Resolución de cobranza\" → Etiqueta: 11, Clasificación: MUY IMPORTANTE\n");
        prompt.append("   - \"Resolución Coactiva\" → Etiqueta: 11, Clasificación: MUY IMPORTANTE\n");
        prompt.append("   - \"Ejecución Coactiva\" → Etiqueta: 11, Clasificación: MUY IMPORTANTE\n");
        prompt.append("   - \"Orden de Pago\" → Etiqueta: 11, Clasificación: MUY IMPORTANTE\n");
        prompt.append("   - \"Notificación SUNAT - Código: 123\" → Etiqueta: 16, Clasificación: RECURRENTE\n");
        prompt.append("   - \"Fiscalización tributaria\" → Etiqueta: 14, Clasificación: MUY IMPORTANTE\n");
        prompt.append("   - \"Resolución administrativa\" → Etiqueta: 13, Clasificación: IMPORTANTE\n");
        prompt.append("   - \"Constancia de pago\" → Etiqueta: 10, Clasificación: RECURRENTE\n\n");
          prompt.append("Responde ÚNICAMENTE en el siguiente formato JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"clasificacion\": \"RECURRENTE\",\n");
        prompt.append("  \"etiqueta_codigo\": \"16\",\n");
        prompt.append("  \"etiqueta_nombre\": \"AVISOS\",\n");
        prompt.append("  \"razon\": \"Explicación breve de por qué se clasificó así\"\n");
        prompt.append("}\n\n");
        prompt.append("IMPORTANTE: La clasificación DEBE seguir las reglas automáticas basadas en la etiqueta detectada.\n");
        prompt.append("NOTA ESPECIAL: Si el asunto o contenido contiene términos como 'Resolución Coactiva', 'Ejecución Coactiva', 'Embargo', 'Retención', 'Orden de Pago', debe clasificarse como RESOLUCIONES DE COBRANZAS (11) - MUY IMPORTANTE.");
        
        return prompt.toString();
    }    /**
     * Construye un prompt mejorado que IGNORA etiquetas previas y se enfoca solo en el contenido
     * ACTUALIZADO: Usa solo las 3 clasificaciones válidas del sistema
     */
    private String buildImprovedPrompt(MensajeSunat mensaje) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("🔍 CLASIFICADOR TRIBUTARIO SUNAT - ANÁLISIS PURO POR CONTENIDO\n\n");
        
        prompt.append("📧 ASUNTO A CLASIFICAR: \"").append(mensaje.getVcAsunto()).append("\"\n\n");
        
        prompt.append("⚠️ IMPORTANTE: IGNORA CUALQUIER ETIQUETA O CLASIFICACIÓN PREVIA\n");
        prompt.append("📝 CLASIFICA ÚNICAMENTE BASÁNDOTE EN EL CONTENIDO DEL ASUNTO\n\n");
          prompt.append("🎯 CLASIFICACIONES DISPONIBLES (4 OPCIONES OBLIGATORIAS):\n\n");
        
        prompt.append("🔴 MUY IMPORTANTE:\n");
        prompt.append("   - Orden de Pago, Resolución Coactiva, Embargo, Cobranza\n");
        prompt.append("   - Fiscalización, Auditoría, Inspección, Multa, Sanción\n");
        prompt.append("   - Términos de URGENCIA: 'URGENTE', 'Vencimiento', 'Plazo crítico'\n");
        prompt.append("   - Etiquetas típicas: 11 (RESOLUCIONES DE COBRANZAS), 14 (FISCALIZACION)\n\n");
        
        prompt.append("🟠 IMPORTANTE:\n");
        prompt.append("   - Resoluciones administrativas, Procedimientos\n");
        prompt.append("   - Inconsistencias CRÍTICAS, Observaciones importantes\n");
        prompt.append("   - Problemas detectados que requieren atención\n");
        prompt.append("   - Etiquetas típicas: 13 (NO CONTENCIOSAS), 15 (ANTERIORES)\n\n");
        
        prompt.append("🟢 INFORMATIVO:\n");
        prompt.append("   - Formularios rutinarios: 'Formulario', 'PDT', 'Planilla Electrónica'\n");
        prompt.append("   - Constancias exitosas: 'Constancia', 'exitosa', 'completado'\n");
        prompt.append("   - Documentos tributarios: 'Factura Electrónica', 'Emisión', 'Comprobante'\n");
        prompt.append("   - Pagos y valores: 'Pago de tributo', 'Declaración'\n");
        prompt.append("   - Etiqueta típica: 10 (VALORES)\n\n");
        
        prompt.append("⚪ RECURRENTE:\n");
        prompt.append("   - Notificaciones informativas generales: 'Notificación SUNAT - Código'\n");
        prompt.append("   - Avisos simples: 'Aviso', 'Recordatorio', 'Información general'\n");
        prompt.append("   - Casos muy generales sin contenido específico\n");
        prompt.append("   - Etiquetas típicas: 00 (NO ETIQUETADOS), 16 (AVISOS)\n\n");
          prompt.append("📋 EJEMPLOS CORRECTOS:\n");
        prompt.append("'URGENTE - Orden de Pago No: 123456' → MUY IMPORTANTE (11)\n");
        prompt.append("'Formulario 0621 - PDT IGV-RENTA MENSUAL' → INFORMATIVO (10)\n");
        prompt.append("'Formulario 0601 con inconsistencias críticas' → IMPORTANTE (13)\n");
        prompt.append("'Resolución Coactiva Levantamiento de Embargo' → MUY IMPORTANTE (11)\n");
        prompt.append("'Constancia de presentación exitosa' → INFORMATIVO (10)\n");
        prompt.append("'Notificación SUNAT - Código: 123' → RECURRENTE (16)\n");
        prompt.append("'Resolución administrativa pendiente' → IMPORTANTE (13)\n");
        prompt.append("'Pago de tributo - Declaración mensual' → INFORMATIVO (10)\n\n");
          prompt.append("📤 RESPONDE EXACTAMENTE EN ESTE FORMATO JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"clasificacion\": \"INFORMATIVO\",\n");
        prompt.append("  \"etiqueta_codigo\": \"10\",\n");
        prompt.append("  \"etiqueta_nombre\": \"VALORES\",\n");
        prompt.append("  \"razon\": \"Formulario tributario rutinario para procesamiento\"\n");
        prompt.append("}\n\n");
          prompt.append("⚡ REGLAS IMPORTANTES:\n");
        prompt.append("- Solo usa: MUY IMPORTANTE, IMPORTANTE, INFORMATIVO o RECURRENTE\n");
        prompt.append("- Formularios rutinarios = INFORMATIVO\n");
        prompt.append("- Constancias exitosas = INFORMATIVO\n");
        prompt.append("- Documentos tributarios = INFORMATIVO\n");
        prompt.append("- Problemas críticos = IMPORTANTE\n");
        prompt.append("- Cobranzas/Fiscalizaciones = MUY IMPORTANTE\n");
        prompt.append("- Avisos generales = RECURRENTE\n\n");
        
        prompt.append("⚡ ANALIZA AHORA EL ASUNTO E IDENTIFICA LAS PALABRAS CLAVE PRINCIPALES.");
        
        return prompt.toString();
    }

    /**
     * Llama a la API de Gemini AI con retry y backoff
     */
    private String callGeminiAPIWithRetry(String prompt) throws Exception {
        int maxRetries = 3;
        int baseDelay = 1000; // 1 segundo
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return callGeminiAPI(prompt);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    // Error 429: Quota exceeded
                    if (attempt == maxRetries) {
                        throw e; // Re-lanzar en el último intento
                    }
                    
                    // Calcular delay exponencial
                    int delay = baseDelay * (int) Math.pow(2, attempt - 1);
                    log.warn("⚠️ Error 429 en intento {}/{}. Esperando {} ms antes del retry...", 
                            attempt, maxRetries, delay);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrumpido durante retry", ie);
                    }
                } else {
                    // Otros errores HTTP, no reintentar
                    throw e;
                }
            }
        }
        
        throw new RuntimeException("Máximo número de reintentos alcanzado");
    }    /**
     * Llama a la API de Gemini AI - MÉTODO PÚBLICO PARA DASHBOARD
     */
    public String callGeminiAPI(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        contents.put("parts", List.of(parts));
        requestBody.put("contents", List.of(contents));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        String url = geminiApiUrl + "?key=" + geminiApiKey;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        
        return response.getBody();
    }

    /**
     * Obtiene el estado actual del rate limiter y circuit breaker
     */
    public RateLimiterStatus getRateLimiterStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // Calcular requests en el último minuto
        long recentRequests = requestHistory.stream()
                .filter(timestamp -> ChronoUnit.MINUTES.between(timestamp, now) < 1)
                .count();
        
        boolean circuitOpen = isCircuitBreakerOpen();
        
        return RateLimiterStatus.builder()
                .enabled(geminiEnabled)
                .requestsInLastMinute((int) recentRequests)
                .maxRequestsPerMinute(rateLimitPerMinute)
                .circuitBreakerOpen(circuitOpen)
                .consecutiveFailures(consecutiveFailures.get())
                .circuitBreakerOpenTime(circuitBreakerOpenTime)
                .build();
    }

    /**
     * Parsea la respuesta de Gemini AI
     */
    private EmailAnalysisResult parseGeminiResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode candidates = jsonNode.get("candidates");
            
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String text = parts.get(0).get("text").asText();
                          // Extraer JSON de la respuesta
                        String jsonText = extractJsonFromText(text);
                        JsonNode analysisNode = objectMapper.readTree(jsonText);
                        
                        EmailAnalysisResult result = EmailAnalysisResult.builder()
                                .clasificacion(analysisNode.get("clasificacion").asText())
                                .etiquetaCodigo(analysisNode.get("etiqueta_codigo").asText())
                                .etiquetaNombre(analysisNode.get("etiqueta_nombre").asText())
                                .razon(analysisNode.get("razon").asText())
                                .confianza(0.8) // Valor por defecto
                                .build();                        // Confiar en la clasificación inteligente de Gemini (sin sobreescribir)
                        log.info("✅ Clasificación pura por IA: {} -> {} ({})", 
                                result.getClasificacion(), result.getEtiquetaCodigo(), result.getEtiquetaNombre());
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al parsear respuesta de Gemini: {}", e.getMessage());
        }
        
        return getDefaultClassification();
    }

    /**
     * Extrae JSON de un texto que puede contener texto adicional
     */
    private String extractJsonFromText(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}") + 1;
        if (start >= 0 && end > start) {
            return text.substring(start, end);
        }
        throw new RuntimeException("No se pudo extraer JSON de la respuesta");
    }

    /**
     * Retorna clasificación por defecto cuando Gemini no está disponible
     */
    private EmailAnalysisResult getDefaultClassification() {
        return EmailAnalysisResult.builder()
                .clasificacion("RECURRENTE")
                .etiquetaCodigo("00")
                .etiquetaNombre("NO ETIQUETADOS")
                .razon("Clasificación automática por defecto")
                .confianza(0.5)
                .build();
    }    /**
     * MÉTODO DESHABILITADO - Permitimos que Gemini clasifique libremente
     * Ya no validamos ni corregimos las decisiones de la IA
     */
    /*
    private EmailAnalysisResult validateAndCorrectClassification(EmailAnalysisResult result) {
        String etiquetaCodigo = result.getEtiquetaCodigo();
        String clasificacionOriginal = result.getClasificacion();
        String clasificacionCorrecta = determinarClasificacionPorEtiqueta(etiquetaCodigo);
        
        // Si la clasificación no es correcta según las reglas, corregirla
        if (!clasificacionOriginal.equals(clasificacionCorrecta)) {
            log.warn("⚠️ Corrigiendo clasificación de '{}' a '{}' para etiqueta {}", 
                    clasificacionOriginal, clasificacionCorrecta, etiquetaCodigo);
            
            result.setClasificacion(clasificacionCorrecta);
            result.setRazon(result.getRazon() + " [Clasificación corregida automáticamente según reglas de negocio]");
        }
        
        return result;
    }
    */
    
    /**
     * MÉTODO DESHABILITADO - Ya no forzamos clasificaciones por agrupamiento rígido
     * Gemini decide la clasificación basada en contenido semántico
     */
    /*
    private String determinarClasificacionPorEtiqueta(String etiquetaCodigo) {
        switch (etiquetaCodigo) {
            case "11": // RESOLUCIONES DE COBRANZAS
            case "14": // RESOLUCIONES DE FISCALIZACION
                return "MUY IMPORTANTE";
            case "13": // RESOLUCIONES NO CONTENCIOSAS
            case "15": // RESOLUCIONES ANTERIORES
                return "IMPORTANTE";
            case "00": // NO ETIQUETADOS
            case "10": // VALORES
            case "16": // AVISOS
            default:
                return "RECURRENTE";
        }
    }
    */

    /**
     * Clase interna para el resultado del análisis
     */
    public static class EmailAnalysisResult {
        private String clasificacion;
        private String etiquetaCodigo;
        private String etiquetaNombre;
        private String razon;
        private double confianza;

        public static EmailAnalysisResultBuilder builder() {
            return new EmailAnalysisResultBuilder();
        }

        // Constructor, getters y setters
        public EmailAnalysisResult() {}

        public EmailAnalysisResult(String clasificacion, String etiquetaCodigo, String etiquetaNombre, String razon, double confianza) {
            this.clasificacion = clasificacion;
            this.etiquetaCodigo = etiquetaCodigo;
            this.etiquetaNombre = etiquetaNombre;
            this.razon = razon;
            this.confianza = confianza;
        }

        // Getters y Setters
        public String getClasificacion() { return clasificacion; }
        public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }
        
        public String getEtiquetaCodigo() { return etiquetaCodigo; }
        public void setEtiquetaCodigo(String etiquetaCodigo) { this.etiquetaCodigo = etiquetaCodigo; }
        
        public String getEtiquetaNombre() { return etiquetaNombre; }
        public void setEtiquetaNombre(String etiquetaNombre) { this.etiquetaNombre = etiquetaNombre; }
        
        public String getRazon() { return razon; }
        public void setRazon(String razon) { this.razon = razon; }
        
        public double getConfianza() { return confianza; }
        public void setConfianza(double confianza) { this.confianza = confianza; }

        // Builder pattern
        public static class EmailAnalysisResultBuilder {
            private String clasificacion;
            private String etiquetaCodigo;
            private String etiquetaNombre;
            private String razon;
            private double confianza;

            public EmailAnalysisResultBuilder clasificacion(String clasificacion) {
                this.clasificacion = clasificacion;
                return this;
            }

            public EmailAnalysisResultBuilder etiquetaCodigo(String etiquetaCodigo) {
                this.etiquetaCodigo = etiquetaCodigo;
                return this;
            }

            public EmailAnalysisResultBuilder etiquetaNombre(String etiquetaNombre) {
                this.etiquetaNombre = etiquetaNombre;
                return this;
            }

            public EmailAnalysisResultBuilder razon(String razon) {
                this.razon = razon;
                return this;
            }

            public EmailAnalysisResultBuilder confianza(double confianza) {
                this.confianza = confianza;
                return this;
            }            public EmailAnalysisResult build() {
                return new EmailAnalysisResult(clasificacion, etiquetaCodigo, etiquetaNombre, razon, confianza);
            }
        }
    }

    /**
     * Clase para el estado del rate limiter
     */
    public static class RateLimiterStatus {
        private boolean enabled;
        private int requestsInLastMinute;
        private int maxRequestsPerMinute;
        private boolean circuitBreakerOpen;
        private int consecutiveFailures;
        private LocalDateTime circuitBreakerOpenTime;

        public static RateLimiterStatusBuilder builder() {
            return new RateLimiterStatusBuilder();
        }

        // Constructor, getters y setters
        public RateLimiterStatus() {}

        public RateLimiterStatus(boolean enabled, int requestsInLastMinute, int maxRequestsPerMinute, 
                               boolean circuitBreakerOpen, int consecutiveFailures, 
                               LocalDateTime circuitBreakerOpenTime) {
            this.enabled = enabled;
            this.requestsInLastMinute = requestsInLastMinute;
            this.maxRequestsPerMinute = maxRequestsPerMinute;
            this.circuitBreakerOpen = circuitBreakerOpen;
            this.consecutiveFailures = consecutiveFailures;
            this.circuitBreakerOpenTime = circuitBreakerOpenTime;
        }

        // Getters y Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getRequestsInLastMinute() { return requestsInLastMinute; }
        public void setRequestsInLastMinute(int requestsInLastMinute) { this.requestsInLastMinute = requestsInLastMinute; }
        
        public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
        public void setMaxRequestsPerMinute(int maxRequestsPerMinute) { this.maxRequestsPerMinute = maxRequestsPerMinute; }
        
        public boolean isCircuitBreakerOpen() { return circuitBreakerOpen; }
        public void setCircuitBreakerOpen(boolean circuitBreakerOpen) { this.circuitBreakerOpen = circuitBreakerOpen; }
        
        public int getConsecutiveFailures() { return consecutiveFailures; }
        public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
        
        public LocalDateTime getCircuitBreakerOpenTime() { return circuitBreakerOpenTime; }
        public void setCircuitBreakerOpenTime(LocalDateTime circuitBreakerOpenTime) { this.circuitBreakerOpenTime = circuitBreakerOpenTime; }

        // Builder pattern
        public static class RateLimiterStatusBuilder {
            private boolean enabled;
            private int requestsInLastMinute;
            private int maxRequestsPerMinute;
            private boolean circuitBreakerOpen;
            private int consecutiveFailures;
            private LocalDateTime circuitBreakerOpenTime;

            public RateLimiterStatusBuilder enabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }

            public RateLimiterStatusBuilder requestsInLastMinute(int requestsInLastMinute) {
                this.requestsInLastMinute = requestsInLastMinute;
                return this;
            }

            public RateLimiterStatusBuilder maxRequestsPerMinute(int maxRequestsPerMinute) {
                this.maxRequestsPerMinute = maxRequestsPerMinute;
                return this;
            }

            public RateLimiterStatusBuilder circuitBreakerOpen(boolean circuitBreakerOpen) {
                this.circuitBreakerOpen = circuitBreakerOpen;
                return this;
            }

            public RateLimiterStatusBuilder consecutiveFailures(int consecutiveFailures) {
                this.consecutiveFailures = consecutiveFailures;
                return this;
            }

            public RateLimiterStatusBuilder circuitBreakerOpenTime(LocalDateTime circuitBreakerOpenTime) {
                this.circuitBreakerOpenTime = circuitBreakerOpenTime;
                return this;
            }

            public RateLimiterStatus build() {
                return new RateLimiterStatus(enabled, requestsInLastMinute, maxRequestsPerMinute, 
                                           circuitBreakerOpen, consecutiveFailures, circuitBreakerOpenTime);
            }
        }
    }
}
