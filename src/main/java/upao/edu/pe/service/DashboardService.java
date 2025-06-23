package upao.edu.pe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.repository.MensajeSunatRepositorio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardService {

    @Autowired
    private MensajeSunatRepositorio mensajeSunatRepositorio;

    @Autowired
    private MensajeSunatServicio mensajeSunatServicio;

    @Autowired
    private GeminiAIService geminiAIService;

    /**
     * Obtiene métricas principales del dashboard
     */
    public Map<String, Object> obtenerMetricasPrincipales(String ruc, Integer dias) {
        log.info("🎯 Obteniendo métricas principales para RUC: {} - Período: {} días", ruc, dias);
        
        Map<String, Object> metricas = new HashMap<>();
        
        try {
            // Obtener todos los mensajes del RUC
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            // Total de correos
            int totalCorreos = mensajes.size();
            
            // Correos por clasificación
            Map<String, Long> clasificaciones = mensajes.stream()
                .filter(m -> m.getClasificacion() != null)
                .collect(Collectors.groupingBy(
                    MensajeSunat::getClasificacion, 
                    Collectors.counting()
                ));
            
            // Correos por etiqueta
            Map<String, Long> etiquetas = mensajes.stream()
                .filter(m -> m.getVcCodigoEtiqueta() != null)
                .collect(Collectors.groupingBy(
                    MensajeSunat::getVcCodigoEtiqueta,
                    Collectors.counting()
                ));
            
            // Construir respuesta
            Map<String, Object> resumenGeneral = new HashMap<>();
            resumenGeneral.put("totalCorreos", totalCorreos);
            resumenGeneral.put("correosNuevos", contarCorreosNuevos(mensajes, dias));
            resumenGeneral.put("muyImportantes", clasificaciones.getOrDefault("MUY IMPORTANTE", 0L));
            resumenGeneral.put("importantes", clasificaciones.getOrDefault("IMPORTANTE", 0L));
            resumenGeneral.put("recurrentes", clasificaciones.getOrDefault("RECURRENTE", 0L));
            resumenGeneral.put("multas", etiquetas.getOrDefault("10", 0L)); // Código 10 = VALORES
            resumenGeneral.put("cobranzas", etiquetas.getOrDefault("11", 0L)); // Código 11 = COBRANZAS
            resumenGeneral.put("fiscalizaciones", etiquetas.getOrDefault("14", 0L)); // Código 14 = FISCALIZACIONES
            resumenGeneral.put("tendenciaSemanal", calcularTendencia(mensajes, 7));
            
            metricas.put("resumenGeneral", resumenGeneral);
            
            // Alertas críticas
            Map<String, Object> alertasCriticas = new HashMap<>();
            alertasCriticas.put("multasVencenHoy", 0); // Placeholder - se puede implementar con fechas reales
            alertasCriticas.put("fiscalizacionesPendientes", etiquetas.getOrDefault("14", 0L));
            alertasCriticas.put("resolucionesUrgentes", clasificaciones.getOrDefault("MUY IMPORTANTE", 0L));
            
            metricas.put("alertasCriticas", alertasCriticas);
            
            // Estado del sistema
            Map<String, Object> estadoSistema = new HashMap<>();
            estadoSistema.put("estadoGeminiAI", obtenerEstadoGeminiAI());
            estadoSistema.put("ultimaSincronizacion", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            estadoSistema.put("rendimientoClasificacion", calcularRendimientoClasificacion(mensajes));
            
            metricas.put("estadoSistema", estadoSistema);
            
            log.info("✅ Métricas principales obtenidas exitosamente");
            return metricas;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener métricas principales: {}", e.getMessage());
            throw new RuntimeException("Error al obtener métricas principales", e);
        }
    }

    /**
     * Obtiene datos para gráfico de tendencias
     */
    public Map<String, Object> obtenerTendencias(String ruc, Integer dias) {
        log.info("📈 Obteniendo tendencias para RUC: {} - Período: {} días", ruc, dias);
        
        Map<String, Object> tendencias = new HashMap<>();
        
        try {
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            // Agrupar por fecha y clasificación
            Map<String, Map<String, Long>> tendenciasPorFecha = new LinkedHashMap<>();
            
            // Generar últimos N días
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);
            for (int i = 0; i < dias; i++) {
                LocalDateTime fecha = fechaInicio.plusDays(i);
                String fechaStr = fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                Map<String, Long> clasificacionesPorFecha = new HashMap<>();
                clasificacionesPorFecha.put("MUY IMPORTANTE", 0L);
                clasificacionesPorFecha.put("IMPORTANTE", 0L);
                clasificacionesPorFecha.put("RECURRENTE", 0L);
                
                tendenciasPorFecha.put(fechaStr, clasificacionesPorFecha);
            }
              // Contar mensajes reales por fecha
            for (MensajeSunat mensaje : mensajes) {
                if (mensaje.getClasificacion() != null && mensaje.getVcFechaEnvio() != null) {
                    try {
                        // Parsear fecha del mensaje
                        LocalDateTime fechaMensaje;
                        if (mensaje.getVcFechaEnvio().contains("/")) {
                            fechaMensaje = LocalDateTime.parse(mensaje.getVcFechaEnvio(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                        } else {
                            fechaMensaje = LocalDateTime.parse(mensaje.getVcFechaEnvio(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        
                        String fechaStr = fechaMensaje.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        
                        if (tendenciasPorFecha.containsKey(fechaStr)) {
                            Map<String, Long> clasificaciones = tendenciasPorFecha.get(fechaStr);
                            String clasificacion = mensaje.getClasificacion();
                            clasificaciones.put(clasificacion, clasificaciones.getOrDefault(clasificacion, 0L) + 1);
                        }
                    } catch (Exception e) {
                        log.warn("Error al parsear fecha del mensaje {}: {}", mensaje.getNuCodigoMensaje(), e.getMessage());
                    }
                }
            }
            
            tendencias.put("datos", tendenciasPorFecha);
            tendencias.put("periodo", dias + " días");
            
            log.info("✅ Tendencias obtenidas exitosamente");
            return tendencias;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener tendencias: {}", e.getMessage());
            throw new RuntimeException("Error al obtener tendencias", e);
        }
    }

    /**
     * Obtiene distribución por etiquetas
     */
    public Map<String, Object> obtenerDistribucionEtiquetas(String ruc, Integer dias) {
        log.info("🏷️ Obteniendo distribución por etiquetas para RUC: {} - Período: {} días", ruc, dias);
        
        Map<String, Object> distribucion = new HashMap<>();
        
        try {
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            Map<String, Long> conteoEtiquetas = mensajes.stream()
                .filter(m -> m.getVcCodigoEtiqueta() != null)
                .collect(Collectors.groupingBy(
                    MensajeSunat::getVcCodigoEtiqueta,
                    Collectors.counting()
                ));
            
            // Convertir códigos a nombres descriptivos
            List<Map<String, Object>> datosGrafico = new ArrayList<>();
            
            Map<String, String> nombresEtiquetas = mensajeSunatServicio.obtenerTodasLasEtiquetas();
            
            for (Map.Entry<String, Long> entry : conteoEtiquetas.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("codigo", entry.getKey());
                item.put("nombre", nombresEtiquetas.getOrDefault(entry.getKey(), "Desconocido"));
                item.put("cantidad", entry.getValue());
                item.put("porcentaje", Math.round((entry.getValue() * 100.0) / mensajes.size()));
                item.put("color", obtenerColorPorEtiqueta(entry.getKey()));
                
                datosGrafico.add(item);
            }
            
            // Ordenar por cantidad descendente
            datosGrafico.sort((a, b) -> Long.compare((Long) b.get("cantidad"), (Long) a.get("cantidad")));
            
            distribucion.put("datos", datosGrafico);
            distribucion.put("totalMensajes", mensajes.size());
            
            log.info("✅ Distribución por etiquetas obtenida exitosamente");
            return distribucion;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener distribución por etiquetas: {}", e.getMessage());
            throw new RuntimeException("Error al obtener distribución por etiquetas", e);
        }
    }

    /**
     * Obtiene heatmap de actividad
     */
    public Map<String, Object> obtenerHeatmapActividad(String ruc, Integer semanas) {
        log.info("🔥 Obteniendo heatmap de actividad para RUC: {} - Período: {} semanas", ruc, semanas);
        
        Map<String, Object> heatmap = new HashMap<>();
        
        try {
            // Simular datos de heatmap (7 días x 24 horas)
            List<List<Integer>> datosHeatmap = new ArrayList<>();
            Random random = new Random();
            
            String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
            
            for (int dia = 0; dia < 7; dia++) {
                List<Integer> horasDelDia = new ArrayList<>();
                for (int hora = 0; hora < 24; hora++) {
                    // Simular más actividad en horarios laborales
                    int actividad = 0;
                    if (hora >= 8 && hora <= 17 && dia < 5) { // Horario laboral, días laborables
                        actividad = random.nextInt(10) + 5;
                    } else if (hora >= 6 && hora <= 22) { // Horario extendido
                        actividad = random.nextInt(5);
                    }
                    horasDelDia.add(actividad);
                }
                datosHeatmap.add(horasDelDia);
            }
            
            heatmap.put("datos", datosHeatmap);
            heatmap.put("diasSemana", Arrays.asList(diasSemana));
            heatmap.put("horas", generarHoras());
            heatmap.put("periodo", semanas + " semanas");
            
            log.info("✅ Heatmap de actividad obtenido exitosamente");
            return heatmap;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener heatmap de actividad: {}", e.getMessage());
            throw new RuntimeException("Error al obtener heatmap de actividad", e);
        }
    }

    /**
     * Obtiene alertas activas del sistema
     */
    public List<Map<String, Object>> obtenerAlertasActivas(String ruc) {
        log.info("🚨 Obteniendo alertas activas para RUC: {}", ruc);
        
        List<Map<String, Object>> alertas = new ArrayList<>();
        
        try {
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            // Alerta por correos muy importantes
            long muyImportantes = mensajes.stream()
                .filter(m -> "MUY IMPORTANTE".equals(m.getClasificacion()))
                .count();
            
            if (muyImportantes > 0) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("tipo", "CORREOS_MUY_IMPORTANTES");
                alerta.put("mensaje", "Tienes " + muyImportantes + " correos muy importantes sin revisar");
                alerta.put("prioridad", "CRITICA");
                alerta.put("accion", "Ver Correos");
                alerta.put("icono", "⚠️");
                alerta.put("cantidad", muyImportantes);
                
                alertas.add(alerta);
            }
            
            // Alerta por fiscalizaciones
            long fiscalizaciones = mensajes.stream()
                .filter(m -> "14".equals(m.getVcCodigoEtiqueta()))
                .count();
            
            if (fiscalizaciones > 0) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("tipo", "FISCALIZACIONES_ACTIVAS");
                alerta.put("mensaje", "Tienes " + fiscalizaciones + " fiscalizaciones activas");
                alerta.put("prioridad", "ALTA");
                alerta.put("accion", "Revisar Fiscalizaciones");
                alerta.put("icono", "🔍");
                alerta.put("cantidad", fiscalizaciones);
                
                alertas.add(alerta);
            }
            
            // Alerta por multas y valores
            long multas = mensajes.stream()
                .filter(m -> "10".equals(m.getVcCodigoEtiqueta()))
                .count();
            
            if (multas > 0) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("tipo", "MULTAS_PENDIENTES");
                alerta.put("mensaje", "Tienes " + multas + " multas o valores pendientes");
                alerta.put("prioridad", "ALTA");
                alerta.put("accion", "Ver Multas");
                alerta.put("icono", "💰");
                alerta.put("cantidad", multas);
                
                alertas.add(alerta);
            }
            
            // Alerta por estado de Gemini AI
            String estadoGemini = obtenerEstadoGeminiAI();
            if (!"OPERATIVO".equals(estadoGemini)) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("tipo", "SISTEMA_GEMINI_AI");
                alerta.put("mensaje", "Gemini AI está " + estadoGemini.toLowerCase() + " - Clasificación automática afectada");
                alerta.put("prioridad", "MEDIA");
                alerta.put("accion", "Verificar Sistema");
                alerta.put("icono", "🤖");
                alerta.put("estado", estadoGemini);
                
                alertas.add(alerta);
            }
            
            log.info("✅ Alertas activas obtenidas exitosamente: {} alertas", alertas.size());
            return alertas;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener alertas activas: {}", e.getMessage());
            throw new RuntimeException("Error al obtener alertas activas", e);
        }
    }    /**
     * Obtiene análisis predictivo usando Gemini AI - ¡POTENCIADO CON IA REAL!
     */
    public Map<String, Object> obtenerAnalisisPredictivo(String ruc) {
        log.info("🔮 Obteniendo análisis predictivo REAL con Gemini AI para RUC: {}", ruc);
        
        Map<String, Object> analisis = new HashMap<>();
        
        try {
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            // 🤖 ANÁLISIS REAL CON GEMINI AI
            Map<String, Object> analisisGemini = analizarConGeminiAI(mensajes, ruc);
              if (analisisGemini != null && !analisisGemini.isEmpty()) {
                // Usar análisis real de Gemini AI
                analisis.putAll(analisisGemini);
                log.info("✅ Análisis predictivo obtenido desde Gemini AI");
            } else {
                // Fallback con datos inteligentes basados en datos reales
                analisis = generarAnalisisFallback(mensajes, ruc);
                log.warn("⚠️ Usando análisis fallback - Gemini AI no disponible");
            }
            
            log.info("✅ Análisis predictivo completado exitosamente");
            return analisis;
              } catch (Exception e) {
            log.error("❌ Error al obtener análisis predictivo: {}", e.getMessage());
            // En caso de error, devolver análisis fallback básico
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            return generarAnalisisFallback(mensajes, ruc);
        }
    }

    /**
     * Obtiene comparativo con períodos anteriores
     */
    public Map<String, Object> obtenerComparativoPeriodos(String ruc, Integer periodoActual, Integer periodoAnterior) {
        log.info("📊 Obteniendo comparativo de períodos para RUC: {} - Actual: {} días, Anterior: {} días", 
                 ruc, periodoActual, periodoAnterior);
        
        Map<String, Object> comparativo = new HashMap<>();
        
        try {
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            // Simular comparativo (en implementación real se usarían fechas reales)
            Map<String, Object> periodoActualData = new HashMap<>();
            periodoActualData.put("totalCorreos", mensajes.size());
            periodoActualData.put("muyImportantes", mensajes.stream().filter(m -> "MUY IMPORTANTE".equals(m.getClasificacion())).count());
            periodoActualData.put("fiscalizaciones", mensajes.stream().filter(m -> "14".equals(m.getVcCodigoEtiqueta())).count());
            
            Map<String, Object> periodoAnteriorData = new HashMap<>();
            periodoAnteriorData.put("totalCorreos", Math.max(0, mensajes.size() - 5)); // Simular período anterior
            periodoAnteriorData.put("muyImportantes", Math.max(0, (long)(mensajes.stream().filter(m -> "MUY IMPORTANTE".equals(m.getClasificacion())).count() - 2)));
            periodoAnteriorData.put("fiscalizaciones", Math.max(0, (long)(mensajes.stream().filter(m -> "14".equals(m.getVcCodigoEtiqueta())).count() - 1)));
            
            // Calcular variaciones
            Map<String, Object> variaciones = new HashMap<>();
            variaciones.put("totalCorreos", calcularVariacion((Integer)periodoActualData.get("totalCorreos"), (Integer)periodoAnteriorData.get("totalCorreos")));
            variaciones.put("muyImportantes", calcularVariacion(((Long)periodoActualData.get("muyImportantes")).intValue(), ((Long)periodoAnteriorData.get("muyImportantes")).intValue()));
            variaciones.put("fiscalizaciones", calcularVariacion(((Long)periodoActualData.get("fiscalizaciones")).intValue(), ((Long)periodoAnteriorData.get("fiscalizaciones")).intValue()));
            
            comparativo.put("periodoActual", periodoActualData);
            comparativo.put("periodoAnterior", periodoAnteriorData);
            comparativo.put("variaciones", variaciones);
            
            log.info("✅ Comparativo de períodos obtenido exitosamente");
            return comparativo;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener comparativo de períodos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener comparativo de períodos", e);
        }
    }

    /**
     * Obtiene ranking de tipos de notificaciones más frecuentes
     */
    public List<Map<String, Object>> obtenerRankingNotificaciones(String ruc, Integer dias) {
        log.info("🏆 Obteniendo ranking de notificaciones para RUC: {} - Período: {} días", ruc, dias);
        
        try {
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            Map<String, String> nombresEtiquetas = mensajeSunatServicio.obtenerTodasLasEtiquetas();
            
            Map<String, Long> conteoEtiquetas = mensajes.stream()
                .filter(m -> m.getVcCodigoEtiqueta() != null)
                .collect(Collectors.groupingBy(
                    MensajeSunat::getVcCodigoEtiqueta,
                    Collectors.counting()
                ));
            
            List<Map<String, Object>> ranking = new ArrayList<>();
            int posicion = 1;
            
            for (Map.Entry<String, Long> entry : conteoEtiquetas.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .collect(Collectors.toList())) {
                
                Map<String, Object> item = new HashMap<>();
                item.put("posicion", posicion++);
                item.put("codigo", entry.getKey());
                item.put("nombre", nombresEtiquetas.getOrDefault(entry.getKey(), "Desconocido"));
                item.put("cantidad", entry.getValue());
                item.put("porcentaje", Math.round((entry.getValue() * 100.0) / mensajes.size()));
                item.put("tendencia", posicion <= 3 ? "⬆️" : "➡️"); // Simular tendencia
                
                ranking.add(item);
            }
            
            log.info("✅ Ranking de notificaciones obtenido exitosamente: {} tipos", ranking.size());
            return ranking;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener ranking de notificaciones: {}", e.getMessage());
            throw new RuntimeException("Error al obtener ranking de notificaciones", e);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================
    
    private int contarCorreosNuevos(List<MensajeSunat> mensajes, int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        
        return (int) mensajes.stream()
            .filter(m -> {
                if (m.getVcFechaEnvio() == null) return false;
                try {
                    LocalDateTime fechaMensaje = parsearFechaMensaje(m.getVcFechaEnvio());
                    return fechaMensaje.isAfter(fechaLimite);
                } catch (Exception e) {
                    return false;
                }
            })
            .count();
    }

    private String calcularTendencia(List<MensajeSunat> mensajes, int dias) {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime inicioSemanaActual = ahora.minusDays(dias);
            LocalDateTime inicioSemanaAnterior = inicioSemanaActual.minusDays(dias);
            
            // Contar mensajes críticos de esta semana
            long mensajesSemanaActual = mensajes.stream()
                .filter(m -> {
                    if (m.getVcFechaEnvio() == null) return false;
                    try {
                        LocalDateTime fechaMensaje = parsearFechaMensaje(m.getVcFechaEnvio());
                        return fechaMensaje.isAfter(inicioSemanaActual);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(m -> "MUY IMPORTANTE".equals(m.getClasificacion()) || "IMPORTANTE".equals(m.getClasificacion()))
                .count();
            
            // Contar mensajes críticos de la semana anterior
            long mensajesSemanaAnterior = mensajes.stream()
                .filter(m -> {
                    if (m.getVcFechaEnvio() == null) return false;
                    try {
                        LocalDateTime fechaMensaje = parsearFechaMensaje(m.getVcFechaEnvio());
                        return fechaMensaje.isAfter(inicioSemanaAnterior) && fechaMensaje.isBefore(inicioSemanaActual);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(m -> "MUY IMPORTANTE".equals(m.getClasificacion()) || "IMPORTANTE".equals(m.getClasificacion()))
                .count();
            
            // Determinar tendencia
            if (mensajesSemanaActual > mensajesSemanaAnterior * 1.2) {
                return "subiendo";
            } else if (mensajesSemanaActual < mensajesSemanaAnterior * 0.8) {
                return "bajando";
            } else {
                return "estable";
            }
            
        } catch (Exception e) {
            log.warn("Error al calcular tendencia: {}", e.getMessage());
            return "estable";
        }
    }
    
    /**
     * Parsea fechas en diferentes formatos
     */
    private LocalDateTime parsearFechaMensaje(String fechaStr) {
        try {
            if (fechaStr.contains("/")) {
                return LocalDateTime.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            } else {
                return LocalDateTime.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        } catch (Exception e) {
            // Si no se puede parsear, asumir fecha muy antigua
            return LocalDateTime.of(2020, 1, 1, 0, 0);
        }
    }

    private String obtenerEstadoGeminiAI() {
        try {
            if (geminiAIService != null) {
                GeminiAIService.RateLimiterStatus status = geminiAIService.getRateLimiterStatus();
                if (status.isCircuitBreakerOpen()) {
                    return "ERROR";
                } else if (status.getRequestsInLastMinute() >= status.getMaxRequestsPerMinute() * 0.8) {
                    return "LIMITADO";
                } else {
                    return "OPERATIVO";
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener estado de Gemini AI: {}", e.getMessage());
        }
        return "DESCONOCIDO";
    }

    private double calcularRendimientoClasificacion(List<MensajeSunat> mensajes) {
        long clasificados = mensajes.stream()
            .filter(m -> m.getClasificacion() != null && !m.getClasificacion().isEmpty())
            .count();
        
        if (mensajes.isEmpty()) return 0.0;
        
        return Math.round((clasificados * 100.0) / mensajes.size());
    }

    private String obtenerColorPorEtiqueta(String codigo) {
        Map<String, String> colores = new HashMap<>();
        colores.put("10", "#FF6B6B"); // Rojo para valores/multas
        colores.put("11", "#4ECDC4"); // Turquesa para cobranzas
        colores.put("13", "#45B7D1"); // Azul para resoluciones
        colores.put("14", "#96CEB4"); // Verde para fiscalizaciones
        colores.put("15", "#FFEAA7"); // Amarillo para resoluciones anteriores
        colores.put("16", "#DDA0DD"); // Morado para avisos
        
        return colores.getOrDefault(codigo, "#95A5A6"); // Gris por defecto
    }

    private List<String> generarHoras() {
        List<String> horas = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            horas.add(String.format("%02d:00", i));
        }
        return horas;
    }

    private Map<String, Object> calcularVariacion(int actual, int anterior) {
        Map<String, Object> variacion = new HashMap<>();
        
        if (anterior == 0) {
            variacion.put("porcentaje", actual > 0 ? 100 : 0);
            variacion.put("tipo", actual > 0 ? "incremento" : "sin_cambio");
        } else {
            double porcentaje = ((actual - anterior) * 100.0) / anterior;
            variacion.put("porcentaje", Math.round(Math.abs(porcentaje)));
            variacion.put("tipo", porcentaje > 0 ? "incremento" : (porcentaje < 0 ? "decremento" : "sin_cambio"));
        }
        
        variacion.put("valor", actual - anterior);
        
        return variacion;
    }
    
    // ==================== MÉTODOS GEMINI AI AVANZADOS ====================
    
    /**
     * 🤖 ANÁLISIS PREDICTIVO REAL CON GEMINI AI
     * Este método hace llamadas intensivas a Gemini AI para obtener insights reales
     */    private Map<String, Object> analizarConGeminiAI(List<MensajeSunat> mensajes, String ruc) {
        Map<String, Object> analisisCompleto = new HashMap<>();
        
        try {
            log.info("🔍 Iniciando análisis con Gemini AI para RUC: {}", ruc);
            
            // 📊 PREPARAR DATOS PARA GEMINI AI
            StringBuilder datosParaAnalisis = new StringBuilder();
            datosParaAnalisis.append("ANÁLISIS TRIBUTARIO SUNAT - RUC: ").append(ruc).append("\n\n");
            
            // Estadísticas básicas
            Map<String, Long> clasificaciones = mensajes.stream()
                .filter(m -> m.getClasificacion() != null)
                .collect(Collectors.groupingBy(MensajeSunat::getClasificacion, Collectors.counting()));
            
            Map<String, Long> etiquetas = mensajes.stream()
                .filter(m -> m.getVcCodigoEtiqueta() != null)
                .collect(Collectors.groupingBy(MensajeSunat::getVcCodigoEtiqueta, Collectors.counting()));
            
            datosParaAnalisis.append("📈 ESTADÍSTICAS ACTUALES:\n");
            datosParaAnalisis.append("- Total de notificaciones: ").append(mensajes.size()).append("\n");
            datosParaAnalisis.append("- Muy importantes: ").append(clasificaciones.getOrDefault("MUY IMPORTANTE", 0L)).append("\n");
            datosParaAnalisis.append("- Importantes: ").append(clasificaciones.getOrDefault("IMPORTANTE", 0L)).append("\n");
            datosParaAnalisis.append("- Recurrentes: ").append(clasificaciones.getOrDefault("RECURRENTE", 0L)).append("\n");
            datosParaAnalisis.append("- Multas/Valores: ").append(etiquetas.getOrDefault("10", 0L)).append("\n");
            datosParaAnalisis.append("- Cobranzas: ").append(etiquetas.getOrDefault("11", 0L)).append("\n");
            datosParaAnalisis.append("- Fiscalizaciones: ").append(etiquetas.getOrDefault("14", 0L)).append("\n");
            
            // Últimos asuntos para análisis de contexto
            datosParaAnalisis.append("\n📋 ÚLTIMOS ASUNTOS IMPORTANTES:\n");
            mensajes.stream()
                .filter(m -> "MUY IMPORTANTE".equals(m.getClasificacion()))
                .limit(5)
                .forEach(m -> datosParaAnalisis.append("- ").append(m.getVcAsunto()).append("\n"));
            
            log.info("📋 Datos preparados para análisis Gemini: {} caracteres", datosParaAnalisis.length());
            
            // 🤖 LLAMADA 1: ANÁLISIS DE PATRONES
            log.info("🔄 Consultando Gemini para patrones...");
            String promptPatrones = construirPromptPatrones(datosParaAnalisis.toString());
            Map<String, Object> patrones = consultarGeminiParaPatrones(promptPatrones);
            if (patrones != null && !patrones.isEmpty()) {
                analisisCompleto.put("patrones", patrones);
                log.info("✅ Patrones obtenidos: {}", patrones.keySet());
            } else {
                log.warn("⚠️ No se obtuvieron patrones de Gemini");
            }
            
            // 🤖 LLAMADA 2: PREDICCIONES ESPECÍFICAS
            log.info("🔄 Consultando Gemini para predicciones...");
            String promptPredicciones = construirPromptPredicciones(datosParaAnalisis.toString());
            Map<String, Object> predicciones = consultarGeminiParaPredicciones(promptPredicciones);
            if (predicciones != null && !predicciones.isEmpty()) {
                analisisCompleto.put("predicciones", predicciones);
                log.info("✅ Predicciones obtenidas: {}", predicciones.keySet());
            } else {
                log.warn("⚠️ No se obtuvieron predicciones de Gemini");
            }
            
            // 🤖 LLAMADA 3: RECOMENDACIONES ACCIONABLES
            log.info("🔄 Consultando Gemini para recomendaciones...");
            String promptRecomendaciones = construirPromptRecomendaciones(datosParaAnalisis.toString());
            List<String> recomendaciones = consultarGeminiParaRecomendaciones(promptRecomendaciones);
            if (recomendaciones != null && !recomendaciones.isEmpty()) {
                analisisCompleto.put("recomendaciones", recomendaciones);
                log.info("✅ Recomendaciones obtenidas: {} items", recomendaciones.size());
            } else {
                log.warn("⚠️ No se obtuvieron recomendaciones de Gemini");
            }
            
            // 🤖 LLAMADA 4: SCORE DE CUMPLIMIENTO
            log.info("🔄 Consultando Gemini para score de cumplimiento...");
            String promptScore = construirPromptScore(datosParaAnalisis.toString());
            Map<String, Object> scoreCompliance = consultarGeminiParaScore(promptScore);
            if (scoreCompliance != null && !scoreCompliance.isEmpty()) {
                analisisCompleto.putAll(scoreCompliance);
                log.info("✅ Score de cumplimiento obtenido: {}", scoreCompliance.keySet());
            } else {
                log.warn("⚠️ No se obtuvo score de cumplimiento de Gemini");
            }
            
            log.info("🎯 Análisis completo con Gemini AI realizado: {} secciones", analisisCompleto.size());
            log.info("📊 Contenido del análisis: {}", analisisCompleto.keySet());
            
            return analisisCompleto;
            
        } catch (Exception e) {
            log.error("❌ Error en análisis con Gemini AI: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 🔮 ANÁLISIS DE PATRONES CON GEMINI AI
     */
    private String construirPromptPatrones(String datos) {
        return "Actúa como experto en análisis tributario peruano y SUNAT. " +
               "Analiza los siguientes datos y identifica PATRONES ESPECÍFICOS:\n\n" + datos + "\n\n" +
               "Responde SOLO en formato JSON con:\n" +
               "{\n" +
               "  \"tendenciaGeneral\": \"descripción de la tendencia principal\",\n" +
               "  \"tipoMasFrecuente\": \"tipo de notificación más común\",\n" +
               "  \"nivelRiesgoActual\": \"BAJO|MEDIO|ALTO\",\n" +
               "  \"observacionesClave\": \"insights importantes detectados\"\n" +
               "}";
    }
      private Map<String, Object> consultarGeminiParaPatrones(String prompt) {
        try {
            log.debug("🔄 Iniciando consulta Gemini para patrones...");
            if (geminiAIService != null) {
                String respuesta = geminiAIService.callGeminiAPI(prompt);
                log.debug("📥 Respuesta Gemini patrones recibida: {} caracteres", respuesta != null ? respuesta.length() : 0);
                Map<String, Object> resultado = procesarRespuestaGeminiComoJSON(respuesta);
                log.debug("✅ Resultado patrones procesado: {} claves", resultado.size());
                return resultado;
            } else {
                log.warn("⚠️ GeminiAIService es null");
            }
        } catch (Exception e) {
            log.warn("⚠️ Error en consulta Gemini para patrones: {}", e.getMessage(), e);
        }
        return new HashMap<>();
    }
    
    /**
     * 📊 PREDICCIONES ESPECÍFICAS CON GEMINI AI
     */
    private String construirPromptPredicciones(String datos) {
        return "Como experto en tributación peruana, analiza estos datos SUNAT y genera PREDICCIONES ESPECÍFICAS:\n\n" + datos + "\n\n" +
               "Responde SOLO en formato JSON con predicciones específicas:\n" +
               "{\n" +
               "  \"proximaSemana\": \"predicción para los próximos 7 días\",\n" +
               "  \"alertaFiscalizacion\": \"probabilidad y timing de fiscalización\",\n" +
               "  \"tendenciaMultas\": \"evolución esperada de multas\",\n" +
               "  \"recomendacionUrgente\": \"acción más crítica a tomar\"\n" +
               "}";
    }
      private Map<String, Object> consultarGeminiParaPredicciones(String prompt) {
        try {
            log.debug("🔄 Iniciando consulta Gemini para predicciones...");
            if (geminiAIService != null) {
                String respuesta = geminiAIService.callGeminiAPI(prompt);
                log.debug("📥 Respuesta Gemini predicciones recibida: {} caracteres", respuesta != null ? respuesta.length() : 0);
                Map<String, Object> resultado = procesarRespuestaGeminiComoJSON(respuesta);
                log.debug("✅ Resultado predicciones procesado: {} claves", resultado.size());
                return resultado;
            } else {
                log.warn("⚠️ GeminiAIService es null");
            }
        } catch (Exception e) {
            log.warn("⚠️ Error en consulta Gemini para predicciones: {}", e.getMessage(), e);
        }
        return new HashMap<>();
    }
    
    /**
     * 💡 RECOMENDACIONES ACCIONABLES CON GEMINI AI
     */
    private String construirPromptRecomendaciones(String datos) {
        return "Como consultor tributario senior especializado en SUNAT, analiza estos datos y proporciona RECOMENDACIONES ACCIONABLES:\n\n" + datos + "\n\n" +
               "Genera exactamente 4 recomendaciones específicas y accionables en formato JSON:\n" +
               "{\n" +
               "  \"recomendaciones\": [\n" +
               "    \"Recomendación específica 1 con acción clara\",\n" +
               "    \"Recomendación específica 2 con acción clara\",\n" +
               "    \"Recomendación específica 3 con acción clara\",\n" +
               "    \"Recomendación específica 4 con acción clara\"\n" +
               "  ]\n" +
               "}";
    }
      private List<String> consultarGeminiParaRecomendaciones(String prompt) {
        try {
            log.debug("🔄 Iniciando consulta Gemini para recomendaciones...");
            if (geminiAIService != null) {
                String respuesta = geminiAIService.callGeminiAPI(prompt);
                log.debug("📥 Respuesta Gemini recomendaciones recibida: {} caracteres", respuesta != null ? respuesta.length() : 0);
                Map<String, Object> json = procesarRespuestaGeminiComoJSON(respuesta);
                if (json != null && json.containsKey("recomendaciones")) {
                    @SuppressWarnings("unchecked")
                    List<String> recomendaciones = (List<String>) json.get("recomendaciones");
                    log.debug("✅ Recomendaciones procesadas: {} items", recomendaciones != null ? recomendaciones.size() : 0);
                    return recomendaciones;                } else {
                    log.warn("⚠️ JSON no contiene clave 'recomendaciones': {}", json != null ? json.keySet() : "null");
                }
            } else {
                log.warn("⚠️ GeminiAIService es null");
            }
        } catch (Exception e) {
            log.warn("⚠️ Error en consulta Gemini para recomendaciones: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }
    
    /**
     * 🏆 SCORE DE CUMPLIMIENTO CON GEMINI AI
     */
    private String construirPromptScore(String datos) {
        return "Como auditor tributario experto en SUNAT, evalúa el CUMPLIMIENTO TRIBUTARIO basado en estos datos:\n\n" + datos + "\n\n" +
               "Calcula un score de cumplimiento del 0-100 y nivel de riesgo. Responde en JSON:\n" +
               "{\n" +
               "  \"scoreCompliance\": 85,\n" +
               "  \"nivelRiesgo\": \"BAJO|MEDIO|ALTO\",\n" +
               "  \"justificacion\": \"explicación del score asignado\",\n" +
               "  \"areasMejora\": \"áreas que necesitan atención\"\n" +
               "}";
    }
      private Map<String, Object> consultarGeminiParaScore(String prompt) {
        try {
            log.debug("🔄 Iniciando consulta Gemini para score...");
            if (geminiAIService != null) {
                String respuesta = geminiAIService.callGeminiAPI(prompt);
                log.debug("📥 Respuesta Gemini score recibida: {} caracteres", respuesta != null ? respuesta.length() : 0);
                Map<String, Object> resultado = procesarRespuestaGeminiComoJSON(respuesta);
                log.debug("✅ Resultado score procesado: {} claves", resultado.size());
                return resultado;
            } else {
                log.warn("⚠️ GeminiAIService es null");
            }
        } catch (Exception e) {
            log.warn("⚠️ Error en consulta Gemini para score: {}", e.getMessage(), e);
        }
        return new HashMap<>();
    }
      /**
     * 🔧 PROCESADOR DE RESPUESTAS JSON DE GEMINI
     */
    private Map<String, Object> procesarRespuestaGeminiComoJSON(String respuestaGemini) {
        try {
            log.debug("🔍 Procesando respuesta de Gemini: {}", respuestaGemini != null ? respuestaGemini.substring(0, Math.min(100, respuestaGemini.length())) + "..." : "null");
            
            if (respuestaGemini != null && !respuestaGemini.trim().isEmpty()) {
                // Extraer JSON de la respuesta de Gemini
                String jsonLimpio = extraerJSONDeRespuesta(respuestaGemini);
                
                if (jsonLimpio != null && !jsonLimpio.isEmpty()) {
                    log.debug("🧹 JSON extraído: {}", jsonLimpio.substring(0, Math.min(200, jsonLimpio.length())) + "...");
                    
                    // Usar ObjectMapper para parsear JSON
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resultado = mapper.readValue(jsonLimpio, Map.class);
                    
                    log.debug("✅ JSON parseado exitosamente, claves: {}", resultado.keySet());
                    return resultado;
                } else {
                    log.warn("⚠️ No se pudo extraer JSON válido de la respuesta");
                }
            } else {
                log.warn("⚠️ Respuesta de Gemini vacía o nula");
            }
        } catch (Exception e) {
            log.warn("⚠️ Error procesando JSON de Gemini: {} - Respuesta: {}", e.getMessage(), 
                    respuestaGemini != null ? respuestaGemini.substring(0, Math.min(200, respuestaGemini.length())) : "null");
        }
        return new HashMap<>();
    }
      /**
     * 🧹 EXTRACTOR DE JSON LIMPIO DE RESPUESTAS DE GEMINI
     */
    private String extraerJSONDeRespuesta(String respuesta) {
        // Gemini a veces incluye texto adicional, extraemos solo el JSON
        try {
            if (respuesta == null || respuesta.trim().isEmpty()) {
                log.warn("⚠️ Respuesta vacía para extraer JSON");
                return "{}";
            }
            
            // Buscar el primer { y el último }
            int inicioJSON = respuesta.indexOf("{");
            int finJSON = respuesta.lastIndexOf("}") + 1;
            
            if (inicioJSON >= 0 && finJSON > inicioJSON) {
                String jsonExtraido = respuesta.substring(inicioJSON, finJSON);
                log.debug("🔍 JSON extraído de posición {} a {}: {}", inicioJSON, finJSON, 
                         jsonExtraido.substring(0, Math.min(100, jsonExtraido.length())) + "...");
                return jsonExtraido;
            } else {
                log.warn("⚠️ No se encontró estructura JSON válida en la respuesta");
                // Intentar buscar patrones alternativos
                if (respuesta.contains("\"")) {
                    // Crear un JSON simple con la respuesta como texto
                    return "{\"respuesta\": \"" + respuesta.replace("\"", "\\\"").replace("\n", "\\n") + "\"}";
                }
            }
        } catch (Exception e) {
            log.warn("❌ Error extrayendo JSON: {} - Respuesta: {}", e.getMessage(), 
                    respuesta != null ? respuesta.substring(0, Math.min(100, respuesta.length())) : "null");
        }
        
        // Si no se puede extraer JSON, devolver JSON vacío válido
        return "{}";
    }
    
    /**
     * 🔄 FALLBACK INTELIGENTE SI GEMINI NO ESTÁ DISPONIBLE
     */
    private Map<String, Object> generarAnalisisFallback(List<MensajeSunat> mensajes, String ruc) {
        Map<String, Object> analisis = new HashMap<>();
        
        // Análisis básico basado en datos reales sin IA
        Map<String, Long> clasificaciones = mensajes.stream()
            .filter(m -> m.getClasificacion() != null)
            .collect(Collectors.groupingBy(MensajeSunat::getClasificacion, Collectors.counting()));
        
        Map<String, Long> etiquetas = mensajes.stream()
            .filter(m -> m.getVcCodigoEtiqueta() != null)
            .collect(Collectors.groupingBy(MensajeSunat::getVcCodigoEtiqueta, Collectors.counting()));
        
        // Patrones básicos
        Map<String, Object> patrones = new HashMap<>();
        long muyImportantes = clasificaciones.getOrDefault("MUY IMPORTANTE", 0L);
        long fiscalizaciones = etiquetas.getOrDefault("14", 0L);
        long multas = etiquetas.getOrDefault("10", 0L);
        
        if (muyImportantes > 5) {
            patrones.put("tendenciaGeneral", "Alto volumen de notificaciones críticas detectado");
        } else if (fiscalizaciones > 0) {
            patrones.put("tendenciaGeneral", "Proceso de fiscalización activo");
        } else {
            patrones.put("tendenciaGeneral", "Actividad tributaria dentro de parámetros normales");
        }
        
        patrones.put("tipoMasFrecuente", obtenerTipoMasFrecuente(etiquetas));
        patrones.put("nivelRiesgoActual", calcularNivelRiesgo(muyImportantes, fiscalizaciones, multas));
        
        analisis.put("patrones", patrones);
        
        // Predicciones básicas
        Map<String, Object> predicciones = new HashMap<>();
        predicciones.put("proximaSemana", "Se esperan " + Math.max(2, mensajes.size() / 10) + " notificaciones nuevas");
        predicciones.put("alertaFiscalizacion", fiscalizaciones > 0 ? "Fiscalización en curso - seguimiento requerido" : "Sin indicios de fiscalización próxima");
        predicciones.put("tendenciaMultas", multas > 2 ? "Atención: múltiples multas activas" : "Situación estable en multas");
        
        analisis.put("predicciones", predicciones);
        
        // Recomendaciones básicas
        List<String> recomendaciones = new ArrayList<>();
        if (muyImportantes > 3) {
            recomendaciones.add("Revisar urgentemente las " + muyImportantes + " notificaciones muy importantes");
        }
        if (fiscalizaciones > 0) {
            recomendaciones.add("Preparar documentación para proceso de fiscalización activo");
        }
        if (multas > 1) {
            recomendaciones.add("Evaluar cronograma de pagos para multas pendientes");
        }
        recomendaciones.add("Mantener documentación contable actualizada");
        
        analisis.put("recomendaciones", recomendaciones);
        
        // Score básico
        int score = calcularScoreBasico(muyImportantes, fiscalizaciones, multas, mensajes.size());
        analisis.put("scoreCompliance", score);
        analisis.put("nivelRiesgo", score >= 80 ? "BAJO" : (score >= 60 ? "MEDIO" : "ALTO"));
        
        return analisis;
    }
    
    // ==================== MÉTODOS AUXILIARES ADICIONALES ====================
    
    /**
     * Obtiene el tipo de notificación más frecuente
     */
    private String obtenerTipoMasFrecuente(Map<String, Long> etiquetas) {
        Map<String, String> nombresEtiquetas = mensajeSunatServicio.obtenerTodasLasEtiquetas();
        
        return etiquetas.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> nombresEtiquetas.getOrDefault(entry.getKey(), "Desconocido"))
            .orElse("Sin datos suficientes");
    }
    
    /**
     * Calcula el nivel de riesgo basado en métricas
     */
    private String calcularNivelRiesgo(long muyImportantes, long fiscalizaciones, long multas) {
        if (fiscalizaciones > 0 || muyImportantes > 5 || multas > 3) {
            return "ALTO";
        } else if (muyImportantes > 2 || multas > 1) {
            return "MEDIO";
        } else {
            return "BAJO";
        }
    }
    
    /**
     * Calcula score básico de cumplimiento
     */
    private int calcularScoreBasico(long muyImportantes, long fiscalizaciones, long multas, int totalMensajes) {
        int score = 100;
        
        // Penalizaciones
        score -= (int) (fiscalizaciones * 20);  // -20 por cada fiscalización
        score -= (int) (muyImportantes * 5);    // -5 por cada muy importante
        score -= (int) (multas * 10);           // -10 por cada multa
        
        // Bonificación por volumen controlado
        if (totalMensajes < 10) {
            score += 5;
        }
        
        return Math.max(0, Math.min(100, score));
    }    /**
     * Obtiene alertas críticas que requieren atención urgente - MEJORADAS CON DATOS REALES
     * Este método filtra solo las alertas más importantes basadas en etiquetas reales
     */
    public List<Map<String, Object>> obtenerAlertasCriticas(String ruc) {
        log.info("🔥 Obteniendo alertas CRÍTICAS REALES para RUC: {}", ruc);
        
        List<Map<String, Object>> alertasCriticas = new ArrayList<>();
        
        try {
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            // 🔥 ALERTA CRÍTICA: Multas y valores pendientes (etiqueta 10)
            long multasPendientes = mensajes.stream()
                .filter(m -> "10".equals(m.getVcCodigoEtiqueta()))
                .count();
            
            if (multasPendientes > 0) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("id", "MULTAS_PENDIENTES");
                alerta.put("titulo", "Multas y Valores Pendientes");
                alerta.put("mensaje", "Tienes " + multasPendientes + " multas o valores que requieren pago inmediato");
                alerta.put("tipo", "MULTA");
                alerta.put("nivelCriticidad", "CRITICA");
                alerta.put("prioridad", 1);
                alerta.put("accion", "Pagar o Impugnar");
                alerta.put("icono", "�");
                alerta.put("color", "#dc3545"); // Rojo crítico
                alerta.put("cantidad", multasPendientes);
                alerta.put("impacto", "ALTO");
                alerta.put("descripcion", "Las multas impagadas generan intereses diarios. Revisa montos, fechas de vencimiento y opciones de fraccionamiento.");
                
                alertasCriticas.add(alerta);
            }
            
            // 🔥 ALERTA CRÍTICA: Resoluciones de cobranza (etiqueta 11)
            long cobranzasActivas = mensajes.stream()
                .filter(m -> "11".equals(m.getVcCodigoEtiqueta()))
                .count();
            
            if (cobranzasActivas > 0) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("id", "COBRANZAS_ACTIVAS");
                alerta.put("titulo", "Procedimientos de Cobranza Activos");
                alerta.put("mensaje", "Tienes " + cobranzasActivas + " resoluciones de cobranza que pueden derivar en embargo");
                alerta.put("tipo", "COBRANZA");
                alerta.put("nivelCriticidad", "CRITICA");
                alerta.put("prioridad", 2);
                alerta.put("accion", "Verificar Estado");
                alerta.put("icono", "⚖️");
                alerta.put("color", "#dc3545"); // Rojo crítico
                alerta.put("cantidad", cobranzasActivas);
                alerta.put("impacto", "ALTO");
                alerta.put("descripcion", "Los procedimientos de cobranza pueden resultar en embargos de cuentas y bienes. Actúa inmediatamente.");
                
                alertasCriticas.add(alerta);
            }
            
            // 🔥 ALERTA IMPORTANTE: Correos sin etiquetar (etiqueta 00)
            long correosSinEtiquetar = mensajes.stream()
                .filter(m -> "00".equals(m.getVcCodigoEtiqueta()))
                .count();
            
            if (correosSinEtiquetar > 50) { // Solo alertar si hay muchos sin revisar
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("id", "CORREOS_SIN_REVISAR");
                alerta.put("titulo", "Correos Sin Revisar");
                alerta.put("mensaje", "Tienes " + correosSinEtiquetar + " correos sin clasificar que pueden contener información importante");
                alerta.put("tipo", "REVISION");
                alerta.put("nivelCriticidad", "MEDIA");
                alerta.put("prioridad", 3);
                alerta.put("accion", "Revisar y Clasificar");
                alerta.put("icono", "�");
                alerta.put("color", "#ffc107"); // Amarillo
                alerta.put("cantidad", correosSinEtiquetar);
                alerta.put("impacto", "MEDIO");
                alerta.put("descripcion", "Correos sin clasificar pueden contener requerimientos, multas o fiscalizaciones importantes.");
                
                alertasCriticas.add(alerta);
            }
            
            // 🔥 ALERTA PREVENTIVA: Eficiencia del sistema
            double porcentajeEtiquetado = mensajes.isEmpty() ? 0 : 
                ((double) (mensajes.size() - correosSinEtiquetar) / mensajes.size()) * 100;
            
            if (porcentajeEtiquetado < 50) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("id", "BAJA_CLASIFICACION");
                alerta.put("titulo", "Sistema Requiere Atención");
                alerta.put("mensaje", "Solo el " + Math.round(porcentajeEtiquetado) + "% de tus correos están clasificados correctamente");
                alerta.put("tipo", "SISTEMA");
                alerta.put("nivelCriticidad", "MEDIA");
                alerta.put("prioridad", 4);
                alerta.put("accion", "Mejorar Clasificación");
                alerta.put("icono", "⚙️");
                alerta.put("color", "#17a2b8"); // Azul info
                alerta.put("cantidad", Math.round(porcentajeEtiquetado));
                alerta.put("impacto", "MEDIO");
                alerta.put("descripcion", "Una mejor clasificación te ayudará a identificar más rápido correos críticos y urgentes.");
                
                alertasCriticas.add(alerta);
            }
            
            // Ordenar por prioridad (menor número = mayor prioridad)
            alertasCriticas.sort((a, b) -> {
                Integer prioridadA = (Integer) a.get("prioridad");
                Integer prioridadB = (Integer) b.get("prioridad");
                return prioridadA.compareTo(prioridadB);
            });
            
            log.info("✅ Alertas críticas REALES obtenidas exitosamente: {} alertas", alertasCriticas.size());
            return alertasCriticas;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener alertas críticas: {}", e.getMessage());
            throw new RuntimeException("Error al obtener alertas críticas", e);
        }    }
    
    /**
     * Obtiene correos críticos recientes - BASADO EN ETIQUETAS REALES
     */
    public List<Map<String, Object>> obtenerCorreosCriticos(String ruc, Integer dias, Integer limite) {
        log.info("🚨 Obteniendo correos críticos REALES para RUC: {} - Período: {} días, Límite: {}", ruc, dias, limite);
        
        try {
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
            
            // Filtrar correos críticos por ETIQUETAS REALES (no por clasificación IA)
            List<Map<String, Object>> correosCriticos = mensajes.stream()
                .filter(m -> {
                    // Filtrar correos críticos por etiqueta
                    boolean esCritico = "10".equals(m.getVcCodigoEtiqueta()) ||  // VALORES (multas)
                                       "11".equals(m.getVcCodigoEtiqueta()) ||  // COBRANZAS
                                       "14".equals(m.getVcCodigoEtiqueta()) ||  // FISCALIZACIONES
                                       m.getNuLeido() == 0 ||                   // No leídos
                                       m.getNuCantidadArchivos() > 0;           // Con archivos
                    
                    // Filtrar por fecha si está disponible
                    boolean esReciente = true; // Por defecto incluir todos si no hay fecha
                    if (m.getVcFechaEnvio() != null) {
                        try {
                            LocalDateTime fechaMensaje = parsearFechaMensaje(m.getVcFechaEnvio());
                            esReciente = fechaMensaje.isAfter(fechaLimite);
                        } catch (Exception e) {
                            esReciente = true; // Si hay error parseando, incluir de todas formas
                        }
                    }
                    
                    return esCritico && esReciente;
                })
                .sorted((a, b) -> {
                    // Ordenar por prioridad de etiqueta y fecha
                    int prioridadA = getPrioridadPorEtiqueta(a.getVcCodigoEtiqueta());
                    int prioridadB = getPrioridadPorEtiqueta(b.getVcCodigoEtiqueta());
                    
                    if (prioridadA != prioridadB) {
                        return prioridadB - prioridadA; // Mayor prioridad primero
                    }
                    
                    // Si misma prioridad, ordenar por fecha (más reciente primero)
                    try {
                        LocalDateTime fechaA = parsearFechaMensaje(a.getVcFechaEnvio());
                        LocalDateTime fechaB = parsearFechaMensaje(b.getVcFechaEnvio());
                        return fechaB.compareTo(fechaA);
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .limit(limite)
                .map(this::convertirCorreoAMapaActualizado)
                .collect(Collectors.toList());
            
            log.info("✅ Correos críticos REALES obtenidos: {}", correosCriticos.size());
            return correosCriticos;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener correos críticos: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private int getPrioridadPorEtiqueta(String codigoEtiqueta) {
        switch (codigoEtiqueta) {
            case "10": return 5; // VALORES (multas) - máxima prioridad
            case "11": return 4; // COBRANZAS - alta prioridad
            case "14": return 3; // FISCALIZACIONES - media-alta prioridad
            case "12": return 2; // NOTIFICACIONES - media prioridad
            default: return 1;   // Otros - baja prioridad
        }
    }
    
    private Map<String, Object> convertirCorreoAMapaActualizado(MensajeSunat correo) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("codigoMensaje", correo.getNuCodigoMensaje());
        mapa.put("asunto", correo.getVcAsunto());
        mapa.put("fechaEnvio", correo.getVcFechaEnvio());
        
        // Determinar prioridad basada en etiqueta REAL
        String prioridad = determinarPrioridadPorEtiqueta(correo.getVcCodigoEtiqueta());
        mapa.put("prioridad", prioridad);
        
        mapa.put("tipoMensaje", correo.getNuTipoMensaje());
        mapa.put("resumen", generarResumenPorEtiqueta(correo.getVcAsunto(), correo.getVcCodigoEtiqueta()));
        mapa.put("leido", correo.getNuLeido() == 1);
        mapa.put("tieneArchivos", correo.getNuCantidadArchivos() > 0);
        mapa.put("cantidadArchivos", correo.getNuCantidadArchivos());
        mapa.put("accionRecomendada", generarAccionPorEtiqueta(correo.getVcCodigoEtiqueta()));
        mapa.put("codigoEtiqueta", correo.getVcCodigoEtiqueta());
        mapa.put("nombreEtiqueta", obtenerNombreEtiqueta(correo.getVcCodigoEtiqueta()));
        
        return mapa;
    }
    
    private String determinarPrioridadPorEtiqueta(String codigoEtiqueta) {
        switch (codigoEtiqueta) {
            case "10": return "MUY_IMPORTANTE"; // VALORES
            case "11": return "MUY_IMPORTANTE"; // COBRANZAS
            case "14": return "IMPORTANTE";     // FISCALIZACIONES
            case "12": return "IMPORTANTE";     // NOTIFICACIONES
            default: return "RECURRENTE";
        }
    }
    
    private String generarResumenPorEtiqueta(String asunto, String codigoEtiqueta) {
        if (asunto == null) return "Correo sin asunto";
        
        switch (codigoEtiqueta) {
            case "10":
                return "💰 Multa o valor pendiente que requiere pago. Revisar monto y fecha de vencimiento.";
            case "11":
                return "📋 Resolución de cobranza activa. Verificar estado del procedimiento y opciones de pago.";
            case "14":
                return "🔍 Proceso de fiscalización en curso. Preparar documentación solicitada.";
            case "12":
                return "📬 Notificación oficial de SUNAT. Revisar contenido y plazos establecidos.";
            default:
                return "📧 Correo importante de SUNAT que requiere revisión.";
        }
    }
    
    private String generarAccionPorEtiqueta(String codigoEtiqueta) {
        switch (codigoEtiqueta) {
            case "10": return "Revisar y Pagar";
            case "11": return "Verificar Estado";
            case "14": return "Preparar Documentos";
            case "12": return "Leer Notificación";
            default: return "Revisar Contenido";
        }
    }
    
    private String obtenerNombreEtiqueta(String codigoEtiqueta) {
        switch (codigoEtiqueta) {
            case "10": return "VALORES";
            case "11": return "COBRANZAS";
            case "14": return "FISCALIZACIONES";
            case "12": return "NOTIFICACIONES";
            case "00": return "SIN ETIQUETAR";
            default: return "OTROS";
        }
    }
    
    private int getPrioridadNumerica(String clasificacion) {
        switch (clasificacion) {
            case "MUY IMPORTANTE": return 3;
            case "IMPORTANTE": return 2;
            case "RECURRENTE": return 1;
            default: return 0;
        }
    }
    
    private Map<String, Object> convertirCorreoAMapa(MensajeSunat correo) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("codigoMensaje", correo.getNuCodigoMensaje());
        mapa.put("asunto", correo.getVcAsunto());
        mapa.put("fechaEnvio", correo.getVcFechaEnvio());
        mapa.put("prioridad", correo.getClasificacion() != null ? correo.getClasificacion().replace(" ", "_") : "IMPORTANTE");
        mapa.put("tipoMensaje", correo.getNuTipoMensaje());
        mapa.put("resumen", generarResumenInteligente(correo.getVcAsunto()));
        mapa.put("leido", correo.getNuLeido() == 1);
        mapa.put("tieneArchivos", correo.getNuCantidadArchivos() > 0);
        mapa.put("cantidadArchivos", correo.getNuCantidadArchivos());
        mapa.put("accionRecomendada", generarAccionRecomendada(correo.getVcAsunto()));
        return mapa;
    }
    
    private String generarResumenInteligente(String asunto) {
        if (asunto == null) return "Correo sin asunto";
        
        String asuntoLower = asunto.toLowerCase();
        
        if (asuntoLower.contains("coactiva") || asuntoLower.contains("embargo")) {
            return "Procedimiento coactivo en curso. Requiere atención inmediata para evitar medidas cautelares.";
        }
        
        if (asuntoLower.contains("fiscalización") || asuntoLower.contains("auditoría")) {
            return "Proceso de fiscalización tributaria. Revisar documentación y plazos de presentación.";
        }
        
        if (asuntoLower.contains("multa") || asuntoLower.contains("sanción")) {
            return "Notificación de multa o sanción tributaria. Evaluar opciones de descargo o pago.";
        }
        
        if (asuntoLower.contains("citación")) {
            return "Citación de la administración tributaria. Presentarse en fecha indicada con documentación.";
        }
        
        return asunto.length() > 100 ? asunto.substring(0, 100) + "..." : asunto;
    }
    
    private String generarAccionRecomendada(String asunto) {
        if (asunto == null) return "Revisar contenido del correo";
        
        String asuntoLower = asunto.toLowerCase();
        
        if (asuntoLower.contains("coactiva")) {
            return "Consultar con asesor tributario y realizar pago pendiente";
        }
        
        if (asuntoLower.contains("fiscalización")) {
            return "Revisar observaciones y presentar descargos si es necesario";
        }
        
        if (asuntoLower.contains("citación")) {
            return "Presentarse en la fecha indicada con documentación";
        }
        
        return "Revisar y dar seguimiento según corresponda";
    }

    /**
     * Obtiene distribución por clasificaciones
     */
    public Map<String, Object> obtenerDistribucionClasificacion(String ruc, Integer dias) {
        log.info("🎯 Obteniendo distribución por clasificaciones para RUC: {} - Período: {} días", ruc, dias);
        
        Map<String, Object> distribucion = new HashMap<>();
        
        try {
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            // Contar mensajes por clasificación
            Map<String, Long> conteoClasificaciones = mensajes.stream()
                .collect(Collectors.groupingBy(
                    m -> m.getClasificacion() != null ? m.getClasificacion() : "SIN CLASIFICAR",
                    Collectors.counting()
                ));
            
            // Convertir a formato para gráficos
            List<Map<String, Object>> datosGrafico = new ArrayList<>();
            
            for (Map.Entry<String, Long> entry : conteoClasificaciones.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("codigo", entry.getKey());
                item.put("nombre", entry.getKey());
                item.put("cantidad", entry.getValue());
                item.put("porcentaje", Math.round((entry.getValue() * 100.0) / mensajes.size()));
                item.put("color", obtenerColorPorClasificacion(entry.getKey()));
                
                datosGrafico.add(item);
            }
            
            // Ordenar por prioridad: MUY IMPORTANTE > IMPORTANTE > INFORMATIVO > RECURRENTE > SIN CLASIFICAR
            datosGrafico.sort((a, b) -> {
                String nombreA = (String) a.get("nombre");
                String nombreB = (String) b.get("nombre");
                return Integer.compare(obtenerPrioridadClasificacion(nombreA), obtenerPrioridadClasificacion(nombreB));
            });
            
            distribucion.put("datos", datosGrafico);
            distribucion.put("totalMensajes", mensajes.size());
            
            log.info("✅ Distribución por clasificaciones obtenida exitosamente");
            return distribucion;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener distribución por clasificaciones: {}", e.getMessage());
            throw new RuntimeException("Error al obtener distribución por clasificaciones", e);
        }
    }

    /**
     * Obtiene color asociado a cada clasificación
     */
    private String obtenerColorPorClasificacion(String clasificacion) {
        switch (clasificacion) {
            case "MUY IMPORTANTE":
                return "#E74C3C"; // Rojo
            case "IMPORTANTE":
                return "#F39C12"; // Naranja
            case "INFORMATIVO":
                return "#27AE60"; // Verde
            case "RECURRENTE":
                return "#3498DB"; // Azul
            case "SIN CLASIFICAR":
            default:
                return "#95A5A6"; // Gris
        }
    }

    /**
     * Obtiene prioridad numérica para ordenamiento
     */
    private int obtenerPrioridadClasificacion(String clasificacion) {
        switch (clasificacion) {
            case "MUY IMPORTANTE":
                return 1;
            case "IMPORTANTE":
                return 2;
            case "INFORMATIVO":
                return 3;
            case "RECURRENTE":
                return 4;
            case "SIN CLASIFICAR":
            default:
                return 5;
        }
    }

    /**
     * Obtiene métricas principales del dashboard (simulación con datos estáticos)
     */
    public Map<String, Object> obtenerMetricasPrincipalesSimuladas(String ruc, Integer dias) {
        log.info("🎯 Obteniendo métricas principales SIMULADAS para RUC: {} - Período: {} días", ruc, dias);
        
        Map<String, Object> metricas = new HashMap<>();
        
        try {
            // Datos simulados
            metricas.put("totalCorreos", 150);
            metricas.put("correosNuevos", 30);
            metricas.put("muyImportantes", 10);
            metricas.put("importantes", 20);
            metricas.put("recurrentes", 5);
            metricas.put("multas", 3);
            metricas.put("cobranzas", 2);
            metricas.put("fiscalizaciones", 1);
            
            // Tendencia simulada
            Map<String, Object> tendencia = new HashMap<>();
            tendencia.put("semanaActual", 50);
            tendencia.put("semanaPasada", 40);
            tendencia.put("variacion", "aumento");
            metricas.put("tendenciaSemanal", tendencia);
            
            // Alertas críticas simuladas
            List<Map<String, Object>> alertas = new ArrayList<>();
            
            Map<String, Object> alerta1 = new HashMap<>();
            alerta1.put("tipo", "CORREOS_MUY_IMPORTANTES");
            alerta1.put("mensaje", "Tienes 5 correos muy importantes sin revisar");
            alerta1.put("prioridad", "CRITICA");
            alerta1.put("accion", "Ver Correos");
            alerta1.put("icono", "⚠️");
            alertas.add(alerta1);
            
            Map<String, Object> alerta2 = new HashMap<>();
            alerta2.put("tipo", "FISCALIZACIONES_ACTIVAS");
            alerta2.put("mensaje", "Tienes 1 fiscalización activa");
            alerta2.put("prioridad", "ALTA");
            alerta2.put("accion", "Revisar Fiscalizaciones");
            alerta2.put("icono", "🔍");
            alertas.add(alerta2);
            
            metricas.put("alertasCriticas", alertas);
            
            log.info("✅ Métricas principales simuladas obtenidas exitosamente");
            return metricas;
            
        } catch (Exception e) {
            log.error("❌ Error al obtener métricas principales simuladas: {}", e.getMessage());
            throw new RuntimeException("Error al obtener métricas principales simuladas", e);
        }
    }
}
