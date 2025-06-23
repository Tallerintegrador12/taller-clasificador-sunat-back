package upao.edu.pe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upao.edu.pe.dto.response.RespuestaControlador;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.service.GeminiAIService;
import upao.edu.pe.service.MensajeSunatServicio;
import upao.edu.pe.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*")
public class GeminiTestController {

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private MensajeSunatServicio mensajeSunatServicio;

    @Autowired
    private NotificationService notificationService;

    /**
     * Endpoint para probar el análisis de un correo específico con Gemini AI
     */
    @PostMapping("/analizar-correo/{id}")
    public ResponseEntity<RespuestaControlador<GeminiAIService.EmailAnalysisResult>> analizarCorreo(
            @PathVariable Long id) {
        
        try {
            // Buscar el correo
            MensajeSunat correo = mensajeSunatServicio.obtenerMensajePorId(id);
            if (correo == null) {
                return ResponseEntity.badRequest().body(
                    new RespuestaControlador<>("Correo no encontrado", 404, null, null)
                );
            }

            // Analizar con Gemini AI
            GeminiAIService.EmailAnalysisResult resultado = geminiAIService.analyzeEmail(correo);

            return ResponseEntity.ok(
                new RespuestaControlador<>("Análisis completado exitosamente", 200, resultado, null)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                new RespuestaControlador<>("Error al analizar correo: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Endpoint para procesar múltiples correos con IA
     */
    @PostMapping("/procesar-correos-lote")
    public ResponseEntity<RespuestaControlador<List<MensajeSunat>>> procesarCorreosEnLote(
            @RequestBody List<Long> idsCorreos) {
        
        try {
            // Obtener correos
            List<MensajeSunat> correos = idsCorreos.stream()
                .map(id -> mensajeSunatServicio.obtenerMensajePorId(id))
                .filter(correo -> correo != null)
                .toList();

            if (correos.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new RespuestaControlador<>("No se encontraron correos válidos", 400, null, null)
                );
            }

            // Procesar con IA
            List<MensajeSunat> correosProcesados = mensajeSunatServicio.procesarNuevosCorreosConIA(correos);

            return ResponseEntity.ok(
                new RespuestaControlador<>("Correos procesados exitosamente", 200, correosProcesados, null)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                new RespuestaControlador<>("Error al procesar correos: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Endpoint para obtener estadísticas de correos procesados
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = mensajeSunatServicio.obtenerEstadisticasCorreos();
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Estadísticas obtenidas exitosamente", 200, estadisticas, null)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                new RespuestaControlador<>("Error al obtener estadísticas: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Endpoint para simular la llegada de nuevos correos (para testing)
     */
    @PostMapping("/simular-nuevos-correos")
    public ResponseEntity<RespuestaControlador<String>> simularNuevosCorreos() {
        try {
            // Obtener algunos correos de ejemplo para simular
            List<MensajeSunat> correosEjemplo = mensajeSunatServicio.obtenerTodosMensajes("20000000001")
                .stream()
                .limit(3)
                .toList();

            if (!correosEjemplo.isEmpty()) {
                // Procesar como si fueran nuevos
                mensajeSunatServicio.procesarNuevosCorreosConIA(correosEjemplo);
                
                return ResponseEntity.ok(
                    new RespuestaControlador<>("Simulación completada. Revisa los logs para ver las notificaciones.", 200, "OK", null)
                );
            } else {
                return ResponseEntity.badRequest().body(
                    new RespuestaControlador<>("No hay correos para simular", 400, null, null)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                new RespuestaControlador<>("Error en simulación: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Endpoint para configurar la API key de Gemini (para testing)
     */
    @PostMapping("/configurar-api-key")
    public ResponseEntity<RespuestaControlador<String>> configurarApiKey(
            @RequestBody Map<String, String> request) {
        
        String apiKey = request.get("apiKey");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new RespuestaControlador<>("API Key no puede estar vacía", 400, null, null)
            );
        }

        // Nota: En un entorno real, deberías almacenar esto de forma segura
        // Por ahora solo confirmamos que se recibió
        
        return ResponseEntity.ok(
            new RespuestaControlador<>("API Key configurada. Actualiza el archivo application.properties", 200, "OK", null)
        );
    }

    /**
     * Endpoint para probar clasificación directa con Gemini AI
     */
    @PostMapping("/clasificar-mensaje")
    public ResponseEntity<RespuestaControlador<GeminiAIService.EmailAnalysisResult>> clasificarMensaje(
            @RequestBody Map<String, Object> request) {
        
        try {            // Crear mensaje temporal para la prueba
            MensajeSunat mensaje = new MensajeSunat();
            mensaje.setVcAsunto((String) request.get("vcAsunto"));
            mensaje.setVcUsuarioEmisor((String) request.get("vcDe"));
            mensaje.setVcFechaPublica((String) request.get("dtFechaRecepcion"));

            // Analizar con Gemini AI
            GeminiAIService.EmailAnalysisResult resultado = geminiAIService.analyzeEmail(mensaje);

            return ResponseEntity.ok(
                new RespuestaControlador<>("Clasificación completada exitosamente", 200, resultado, null)
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new RespuestaControlador<>("Error al clasificar: " + e.getMessage(), 500, null, null)
            );
        }
    }
}
