package upao.edu.pe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upao.edu.pe.dto.response.RespuestaControlador;
import upao.edu.pe.service.DashboardService;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(originPatterns = "*", allowCredentials = "false")
@Tag(name = "Dashboard Analítico", description = "API para métricas y análisis avanzado del sistema")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Obtiene métricas principales del dashboard
     */
    @Operation(summary = "Métricas principales", 
               description = "Obtiene las métricas principales para el dashboard ejecutivo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente"),
        @ApiResponse(responseCode = "400", description = "RUC inválido"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/metricas-principales")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerMetricasPrincipales(
            @Parameter(description = "RUC del contribuyente", required = true)
            @RequestParam("ruc") String ruc,
            @Parameter(description = "Período en días", required = false)
            @RequestParam(value = "dias", defaultValue = "30") Integer dias) {
        
        try {
            Map<String, Object> metricas = dashboardService.obtenerMetricasPrincipales(ruc, dias);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Métricas obtenidas exitosamente", 200, metricas, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener métricas: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene datos para gráfico de tendencias
     */
    @GetMapping("/tendencias")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerTendencias(
            @RequestParam("ruc") String ruc,
            @RequestParam(value = "dias", defaultValue = "30") Integer dias) {
        
        try {
            Map<String, Object> tendencias = dashboardService.obtenerTendencias(ruc, dias);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Tendencias obtenidas exitosamente", 200, tendencias, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener tendencias: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene distribución por etiquetas
     */
    @GetMapping("/distribucion-etiquetas")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerDistribucionEtiquetas(
            @RequestParam("ruc") String ruc,
            @RequestParam(value = "dias", defaultValue = "30") Integer dias) {
        
        try {
            Map<String, Object> distribucion = dashboardService.obtenerDistribucionEtiquetas(ruc, dias);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Distribución obtenida exitosamente", 200, distribucion, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener distribución: " + e.getMessage(), 500, null, null)
            );
        }
    }    /**
     * Obtiene correos críticos recientes
     */
    @Operation(summary = "Correos críticos recientes", 
               description = "Obtiene los correos más críticos de los últimos días")
    @GetMapping("/correos-criticos")
    public ResponseEntity<RespuestaControlador<List<Map<String, Object>>>> obtenerCorreosCriticos(
            @RequestParam("ruc") String ruc,
            @RequestParam(value = "dias", defaultValue = "7") Integer dias,
            @RequestParam(value = "limite", defaultValue = "5") Integer limite) {
        
        try {
            List<Map<String, Object>> correosCriticos = dashboardService.obtenerCorreosCriticos(ruc, dias, limite);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Correos críticos obtenidos exitosamente", 200, correosCriticos, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener correos críticos: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene heatmap de actividad
    @GetMapping("/heatmap-actividad")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerHeatmapActividad(
            @RequestParam("ruc") String ruc,
            @RequestParam(value = "semanas", defaultValue = "4") Integer semanas) {
        
        try {
            Map<String, Object> heatmap = dashboardService.obtenerHeatmapActividad(ruc, semanas);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Heatmap obtenido exitosamente", 200, heatmap, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener heatmap: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene alertas activas del sistema
     */
    @GetMapping("/alertas-activas")
    public ResponseEntity<RespuestaControlador<List<Map<String, Object>>>> obtenerAlertasActivas(
            @RequestParam("ruc") String ruc) {
        
        try {
            List<Map<String, Object>> alertas = dashboardService.obtenerAlertasActivas(ruc);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Alertas obtenidas exitosamente", 200, alertas, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener alertas: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene análisis predictivo usando Gemini AI
     */
    @GetMapping("/analisis-predictivo")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerAnalisisPredictivo(
            @RequestParam("ruc") String ruc) {
        
        try {
            Map<String, Object> analisis = dashboardService.obtenerAnalisisPredictivo(ruc);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Análisis predictivo obtenido exitosamente", 200, analisis, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener análisis predictivo: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene comparativo con períodos anteriores
     */
    @GetMapping("/comparativo-periodos")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerComparativoPeriodos(
            @RequestParam("ruc") String ruc,
            @RequestParam(value = "periodoActual", defaultValue = "30") Integer periodoActual,
            @RequestParam(value = "periodoAnterior", defaultValue = "30") Integer periodoAnterior) {
        
        try {
            Map<String, Object> comparativo = dashboardService.obtenerComparativoPeriodos(
                ruc, periodoActual, periodoAnterior);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Comparativo obtenido exitosamente", 200, comparativo, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener comparativo: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene ranking de tipos de notificaciones más frecuentes
     */
    @GetMapping("/ranking-notificaciones")
    public ResponseEntity<RespuestaControlador<List<Map<String, Object>>>> obtenerRankingNotificaciones(
            @RequestParam("ruc") String ruc,
            @RequestParam(value = "dias", defaultValue = "90") Integer dias) {
        
        try {
            List<Map<String, Object>> ranking = dashboardService.obtenerRankingNotificaciones(ruc, dias);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Ranking obtenido exitosamente", 200, ranking, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener ranking: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene alertas críticas que requieren atención urgente
     * Filtra específicamente las alertas más importantes y urgentes del sistema
     */
    @Operation(summary = "Alertas Críticas", 
               description = "Obtiene alertas críticas y urgentes que requieren atención inmediata del contribuyente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alertas críticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "400", description = "RUC inválido"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/alertas-criticas")
    public ResponseEntity<RespuestaControlador<List<Map<String, Object>>>> obtenerAlertasCriticas(
            @Parameter(description = "RUC del contribuyente", required = true)
            @RequestParam("ruc") String ruc) {
        
        try {
            List<Map<String, Object>> alertasCriticas = dashboardService.obtenerAlertasCriticas(ruc);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Alertas críticas obtenidas exitosamente", 200, alertasCriticas, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener alertas críticas: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene distribución por clasificaciones
     */
    @Operation(summary = "Distribución por clasificaciones", 
               description = "Obtiene la distribución de mensajes por clasificación (MUY IMPORTANTE, IMPORTANTE, INFORMATIVO, RECURRENTE)")
    @GetMapping("/distribucion-clasificacion")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerDistribucionClasificacion(
            @RequestParam("ruc") String ruc,
            @RequestParam(value = "dias", defaultValue = "30") Integer dias) {
        
        try {
            Map<String, Object> distribucion = dashboardService.obtenerDistribucionClasificacion(ruc, dias);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Distribución por clasificación obtenida exitosamente", 200, distribucion, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener distribución por clasificación: " + e.getMessage(), 500, null, null)
            );
        }
    }
}
