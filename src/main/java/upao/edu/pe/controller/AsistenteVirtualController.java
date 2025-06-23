package upao.edu.pe.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import upao.edu.pe.dto.AsistenteConsultaDTO;
import upao.edu.pe.dto.response.AsistenteRespuestaDTO;
import upao.edu.pe.service.AsistenteVirtualService;

/**
 * Controlador REST para el Asistente Virtual Contable
 */
@RestController
@RequestMapping("/api/asistente")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AsistenteVirtualController {

    private final AsistenteVirtualService asistenteService;

    /**
     * Endpoint principal para consultas al asistente virtual
     * 
     * @param consulta DTO con la consulta del usuario
     * @return Respuesta del asistente virtual
     */    @PostMapping("/consultar")
    public Mono<ResponseEntity<AsistenteRespuestaDTO>> procesarConsulta(
            @RequestBody AsistenteConsultaDTO consulta) {
        
        log.info("📩 Nueva consulta recibida para el asistente virtual");
        log.info("📝 Consulta recibida: {}", consulta);
        
        // Validación básica
        if (consulta.getConsulta() == null || consulta.getConsulta().trim().isEmpty()) {
            log.error("❌ Consulta vacía recibida");
            return Mono.just(ResponseEntity.badRequest()
                    .body(AsistenteRespuestaDTO.builder()
                            .respuesta("La consulta no puede estar vacía")
                            .confianza(0.0)
                            .categoria("Error")
                            .build()));
        }
          return asistenteService.procesarConsulta(consulta)
                .map(respuesta -> {
                    log.info("✅ Consulta procesada exitosamente");
                    return ResponseEntity.ok(respuesta);
                })
                .onErrorReturn(ResponseEntity.internalServerError()
                        .body(AsistenteRespuestaDTO.builder()
                                .respuesta("Lo siento, ocurrió un error procesando tu consulta. Por favor, intenta nuevamente.")
                                .confianza(0.0)
                                .categoria("Error")
                                .build()));
    }

    /**
     * Endpoint para obtener el estado del asistente
     */
    @GetMapping("/estado")
    public ResponseEntity<String> obtenerEstado() {
        return ResponseEntity.ok("Asistente Virtual Contable funcionando correctamente ✅");
    }

    /**
     * Endpoint para obtener información sobre las capacidades del asistente
     */
    @GetMapping("/capacidades")
    public ResponseEntity<Object> obtenerCapacidades() {
        return ResponseEntity.ok(java.util.Map.of(
            "descripcion", "Asistente Virtual Contable especializado en tributación peruana",
            "especialidades", java.util.List.of(
                "IGV y facturación electrónica",
                "Impuesto a la Renta",
                "Procedimientos de cobranza coactiva",
                "Declaraciones tributarias",
                "Libros contables electrónicos",
                "Normativa SUNAT actualizada"
            ),
            "idiomas", java.util.List.of("Español"),
            "disponibilidad", "24/7",
            "modelo_ia", "Gemini-1.5-Pro"
        ));
    }

    /**
     * Endpoint para consultas rápidas predefinidas
     */
    @GetMapping("/consultas-frecuentes")
    public ResponseEntity<Object> obtenerConsultasFrecuentes() {
        return ResponseEntity.ok(java.util.Map.of(
            "consultas", java.util.List.of(
                "¿Cuándo vence mi declaración mensual?",
                "¿Cómo calculo el IGV de una factura?",
                "¿Qué documentos necesito para una fiscalización?",
                "¿Cómo evito multas por presentación tardía?",
                "¿Cuáles son las tasas del Impuesto a la Renta?",
                "¿Qué libros contables debo llevar?",
                "¿Cómo funciona la retención del IGV?",
                "¿Qué hacer si recibo una resolución de cobranza coactiva?"
            )
        ));
    }

    /**
     * Endpoint de prueba para debugging
     */
    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestBody String body) {
        log.info("🧪 Test endpoint - Body recibido: {}", body);
        return ResponseEntity.ok("Test exitoso. Body recibido: " + body);
    }
}
