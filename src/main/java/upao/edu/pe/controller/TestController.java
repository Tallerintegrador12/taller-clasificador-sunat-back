package upao.edu.pe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upao.edu.pe.dto.response.RespuestaControlador;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.service.GeminiAIService;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private GeminiAIService geminiAIService;

    /**
     * Endpoint para probar clasificación directa con Gemini
     */
    @PostMapping("/clasificar")
    public ResponseEntity<Map<String, Object>> clasificarCorreo(@RequestBody Map<String, String> request) {
        try {
            String asunto = request.get("vcAsunto");
            String cuerpo = request.getOrDefault("vcCuerpo", "");
            String de = request.getOrDefault("vcDe", "test@sunat.gob.pe");
              // Crear mensaje temporal para prueba
            MensajeSunat mensaje = new MensajeSunat();
            mensaje.setVcAsunto(asunto);
            mensaje.setVcUsuarioEmisor(de);
            mensaje.setDtFechaVigencia(LocalDateTime.now());
            
            // Analizar con Gemini
            GeminiAIService.EmailAnalysisResult resultado = geminiAIService.analyzeEmail(mensaje);
            
            // Retornar resultado en formato simple
            return ResponseEntity.ok(Map.of(
                "clasificacion", resultado.getClasificacion(),
                "etiqueta_codigo", resultado.getEtiquetaCodigo(),
                "etiqueta_nombre", resultado.getEtiquetaNombre(),
                "razon", resultado.getRazon(),
                "confianza", resultado.getConfianza()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al clasificar: " + e.getMessage()
            ));
        }
    }
}