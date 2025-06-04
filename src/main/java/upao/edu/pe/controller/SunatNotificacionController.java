package upao.edu.pe.controller;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upao.edu.pe.dto.NotificacionResponseDto;
import upao.edu.pe.model.DetalleNotificacion;
import upao.edu.pe.service.SunatNotificacionService;
import upao.edu.pe.service.SunatServicio;

import java.util.Optional;

@RestController
@RequestMapping("/api/sunat/notificaciones")
/*@RequiredArgsConstructor
*@CrossOrigin(origins = "*")*/
public class SunatNotificacionController {

    private static final Logger log = (Logger) LoggerFactory.getLogger(SunatNotificacionController.class);

    @Autowired
    private SunatNotificacionService sunatNotificacionService;

    /**
     * Endpoint para procesar una notificación de SUNAT por código de mensaje
     * POST /api/sunat/notificaciones/procesar/{codigoMensaje}
     */
    @PostMapping("/procesar/{codigoMensaje}")
    public ResponseEntity<?> procesarNotificacion(@PathVariable String codigoMensaje) {
        try {
            log.info("Iniciando procesamiento de notificación: {}", codigoMensaje);

            if (codigoMensaje == null || codigoMensaje.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("El código de mensaje es requerido");
            }

            DetalleNotificacion detalle = sunatNotificacionService.procesarYGuardarNotificacion(codigoMensaje);
            NotificacionResponseDto response = sunatNotificacionService.convertirAFormatoSolicitado(detalle);

            log.info("Notificación {} procesada exitosamente", codigoMensaje);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al procesar notificación {}: {}", codigoMensaje, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener/procesar la notificación: " + e.getMessage());
        }
    }

    /**
     * Endpoint para refrecar/actualizar una notificación desde la API de SUNAT
     * PUT /api/sunat/notificaciones/refrescar/{codigoMensaje}
     */
    @PutMapping("/refrescar/{codigoMensaje}")
    public ResponseEntity<?> refrescarNotificacion(@PathVariable String codigoMensaje) {
        try {
            log.info("Refrescando notificación desde API: {}", codigoMensaje);

            if (codigoMensaje == null || codigoMensaje.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("El código de mensaje es requerido");
            }

            // Eliminar registro existente si existe
            Optional<DetalleNotificacion> existente = sunatNotificacionService.buscarDetallePorCodigo(codigoMensaje);
            if (existente.isPresent()) {
                log.info("Eliminando registro existente para actualizar: {}", codigoMensaje);
                // Aquí podrías agregar lógica para eliminar el registro existente si lo deseas
            }

            // Procesar nuevamente desde la API
            DetalleNotificacion detalle = sunatNotificacionService.procesarYGuardarNotificacion(codigoMensaje);
            NotificacionResponseDto response = sunatNotificacionService.convertirAFormatoSolicitado(detalle);

            log.info("Notificación {} refrescada exitosamente", codigoMensaje);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al refrescar notificación {}: {}", codigoMensaje, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al refrescar la notificación: " + e.getMessage());
        }
    }


    /**
     * Endpoint para obtener el detalle de una notificación por código de mensaje
     * GET /api/sunat/notificaciones/{codigoMensaje}
     */
    @GetMapping("/{codigoMensaje}")
    public ResponseEntity<?> obtenerDetalleNotificacion(@PathVariable String codigoMensaje) {
        try {
            log.info("Consultando detalle de notificación: {}", codigoMensaje);

            if (codigoMensaje == null || codigoMensaje.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("El código de mensaje es requerido");
            }

            Optional<DetalleNotificacion> detalleOpt = sunatNotificacionService.buscarDetallePorCodigo(codigoMensaje);

            if (detalleOpt.isEmpty()) {
                log.warn("Notificación no encontrada: {}", codigoMensaje);
                return ResponseEntity.notFound().build();
            }

            NotificacionResponseDto response = sunatNotificacionService.convertirAFormatoSolicitado(detalleOpt.get());

            log.info("Detalle de notificación {} obtenido exitosamente", codigoMensaje);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener detalle de notificación {}: {}", codigoMensaje, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el detalle de la notificación: " + e.getMessage());
        }
    }

    /**
     * Endpoint para obtener o procesar una notificación (busca primero en BD, si no existe la procesa)
     * GET /api/sunat/notificaciones/obtener-o-procesar/{codigoMensaje}
     */
    @GetMapping("/obtener-o-procesar/{codigoMensaje}")
    public ResponseEntity<?> obtenerOProcesarNotificacion(@PathVariable String codigoMensaje) {
        try {
            log.info("Obteniendo o procesando notificación: {}", codigoMensaje);

            if (codigoMensaje == null || codigoMensaje.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("El código de mensaje es requerido");
            }

            // Primero intentar buscar en la base de datos
            Optional<DetalleNotificacion> detalleOpt = sunatNotificacionService.buscarDetallePorCodigo(codigoMensaje);

            DetalleNotificacion detalle;
            if (detalleOpt.isPresent()) {
                log.info("Notificación {} encontrada en base de datos", codigoMensaje);
                detalle = detalleOpt.get();
            } else {
                log.info("Notificación {} no encontrada, procesando desde API", codigoMensaje);
                detalle = sunatNotificacionService.procesarYGuardarNotificacion(codigoMensaje);
            }

            NotificacionResponseDto response = sunatNotificacionService.convertirAFormatoSolicitado(detalle);

            log.info("Notificación {} obtenida/procesada exitosamente", codigoMensaje);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener/procesar notificación {}: {}", codigoMensaje, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el detalle de la notificación: " + e.getMessage());
        }
    }
}