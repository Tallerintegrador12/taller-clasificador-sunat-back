package upao.edu.pe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import upao.edu.pe.dto.AsistenteConsultaDTO;
import upao.edu.pe.dto.response.AsistenteRespuestaDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio del Asistente Virtual Contable
 * Utiliza Gemini AI para responder consultas tributarias especializadas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsistenteVirtualService {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    private static final String PROMPT_SISTEMA = """
        Eres un Contador Público Colegiado especializado en tributación peruana y normativa SUNAT.
        
        PERFIL PROFESIONAL:
        - Experto en tributación peruana con 15+ años de experiencia
        - Especialista en IGV, Impuesto a la Renta, retenciones y percepciones
        - Conocimiento actualizado de normativa SUNAT y jurisprudencia tributaria
        - Experiencia en fiscalizaciones, procedimientos de cobranza coactiva
        
        INSTRUCCIONES DE RESPUESTA:
        1. Responde SOLO consultas relacionadas con tributación peruana y contabilidad
        2. Si la consulta no es tributaria, responde: "Lo siento, solo puedo ayudarte con consultas tributarias y contables relacionadas con SUNAT"
        3. Proporciona respuestas precisas, citando artículos de ley cuando sea relevante
        4. Incluye ejemplos prácticos cuando sea posible
        5. Sugiere acciones concretas que puede tomar el usuario
        6. Mantén un tono profesional pero accesible
        
        ESTRUCTURA DE RESPUESTA:
        - Respuesta directa y clara
        - Base legal (si aplica)
        - Ejemplo práctico (si es útil)
        - Recomendaciones de acción
        
        IMPORTANTE: Si detectas una situación urgente (multas, cobranza coactiva), 
        recomienda consultar presencialmente con un contador o abogado tributarista.
        """;

    /**
     * Procesa una consulta del usuario y genera una respuesta especializada
     */
    public Mono<AsistenteRespuestaDTO> procesarConsulta(AsistenteConsultaDTO consulta) {
        long startTime = System.currentTimeMillis();
        
        log.info("🤖 Procesando consulta del asistente virtual: {}", 
                consulta.getConsulta().substring(0, Math.min(50, consulta.getConsulta().length())));
        
        return construirContexto(consulta)
                .flatMap(this::enviarConsultaAGemini)
                .map(respuesta -> construirRespuesta(respuesta, startTime))
                .doOnSuccess(resp -> log.info("✅ Consulta procesada exitosamente en {}ms", resp.getTiempoRespuesta()))
                .doOnError(error -> log.error("❌ Error procesando consulta: {}", error.getMessage()));
    }
    
    /**
     * Construye el contexto completo para la consulta
     */
    private Mono<String> construirContexto(AsistenteConsultaDTO consulta) {
        StringBuilder contexto = new StringBuilder();
        
        contexto.append(PROMPT_SISTEMA).append("\n\n");
        
        // Agregar contexto adicional si se proporciona
        if (consulta.getContexto() != null && !consulta.getContexto().isEmpty()) {
            contexto.append("CONTEXTO ADICIONAL:\n")
                    .append(consulta.getContexto())
                    .append("\n\n");
        }
        
        // TODO: Agregar historial del usuario si se solicita
        if (consulta.isIncluirHistorial() && consulta.getUsuarioId() != null) {
            // Aquí se podría agregar el historial de notificaciones del usuario
            contexto.append("HISTORIAL RECIENTE: El usuario tiene notificaciones pendientes de SUNAT.\n\n");
        }
        
        contexto.append("CONSULTA DEL USUARIO:\n")
                .append(consulta.getConsulta());
        
        return Mono.just(contexto.toString());
    }
    
    /**
     * Envía la consulta a Gemini AI
     */
    private Mono<String> enviarConsultaAGemini(String promptCompleto) {
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of(
                    "parts", List.of(
                        Map.of("text", promptCompleto)
                    )
                )
            ),
            "generationConfig", Map.of(
                "temperature", 0.3,
                "topK", 20,
                "topP", 0.8,
                "maxOutputTokens", 2048
            )
        );
        
        return webClientBuilder.build()
                .post()
                .uri(apiUrl + "?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extraerRespuestaDeGemini)
                .onErrorMap(error -> {
                    log.error("Error llamando a Gemini API: {}", error.getMessage());
                    return new RuntimeException("Error procesando consulta. Intente nuevamente.");
                });
    }
    
    /**
     * Extrae la respuesta del JSON de Gemini
     */
    @SuppressWarnings("unchecked")
    private String extraerRespuestaDeGemini(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
            throw new RuntimeException("Respuesta inesperada de Gemini API");
        } catch (Exception e) {
            log.error("Error extrayendo respuesta de Gemini: {}", e.getMessage());
            throw new RuntimeException("Error procesando respuesta de IA");
        }
    }
    
    /**
     * Construye la respuesta final del asistente
     */
    private AsistenteRespuestaDTO construirRespuesta(String respuestaIA, long startTime) {
        long tiempoRespuesta = System.currentTimeMillis() - startTime;
        
        return AsistenteRespuestaDTO.builder()
                .respuesta(respuestaIA)
                .confianza(0.85) // Valor fijo por ahora, se puede mejorar
                .categoria(detectarCategoria(respuestaIA))
                .fuentes(List.of("SUNAT", "Código Tributario", "Gemini AI"))
                .recomendaciones(extraerRecomendaciones(respuestaIA))
                .tiempoRespuesta(tiempoRespuesta)
                .timestamp(LocalDateTime.now())
                .requiereSeguimiento(detectarSeguimiento(respuestaIA))
                .sesionId(UUID.randomUUID().toString())
                .build();
    }
    
    /**
     * Detecta la categoría de la consulta basándose en la respuesta
     */
    private String detectarCategoria(String respuesta) {
        String respuestaLower = respuesta.toLowerCase();
        
        if (respuestaLower.contains("igv") || respuestaLower.contains("impuesto general")) {
            return "IGV";
        } else if (respuestaLower.contains("renta") || respuestaLower.contains("impuesto a la renta")) {
            return "Impuesto a la Renta";
        } else if (respuestaLower.contains("coactiv") || respuestaLower.contains("multa")) {
            return "Cobranza Coactiva";
        } else if (respuestaLower.contains("declaraci") || respuestaLower.contains("pdt")) {
            return "Declaraciones";
        } else if (respuestaLower.contains("libro") || respuestaLower.contains("contab")) {
            return "Contabilidad";
        }
        
        return "Consulta General";
    }
    
    /**
     * Extrae recomendaciones clave de la respuesta
     */
    private List<String> extraerRecomendaciones(String respuesta) {
        // Implementación básica - se puede mejorar con NLP
        if (respuesta.toLowerCase().contains("urgente") || respuesta.toLowerCase().contains("multa")) {
            return List.of("Consultar con contador presencialmente", "Revisar fechas de vencimiento");
        }
        
        return List.of("Mantener documentación actualizada", "Revisar calendario tributario");
    }
    
    /**
     * Detecta si la consulta requiere seguimiento especializado
     */
    private boolean detectarSeguimiento(String respuesta) {
        String respuestaLower = respuesta.toLowerCase();
        return respuestaLower.contains("coactiv") || 
               respuestaLower.contains("fiscaliz") || 
               respuestaLower.contains("urgente") ||
               respuestaLower.contains("multa");
    }
}
