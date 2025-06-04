package upao.edu.pe.exception;


import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import upao.edu.pe.service.SunatServicio;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class ManejadorExcepciones {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(ManejadorExcepciones.class);


    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionCliente(RestClientException ex) {


        logger.error("Error al consumir API externa: {}", ex.getMessage(), ex);

        Map<String, Object> cuerpoRespuesta = new HashMap<>();
        cuerpoRespuesta.put("timestamp", LocalDateTime.now().toString());
        cuerpoRespuesta.put("mensaje", "Error al consumir la API externa");
        cuerpoRespuesta.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());

        return new ResponseEntity<>(cuerpoRespuesta, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionGeneral(Exception ex) {
        logger.error("Error no controlado: {}", ex.getMessage(), ex);

        Map<String, Object> cuerpoRespuesta = new HashMap<>();
        cuerpoRespuesta.put("timestamp", LocalDateTime.now().toString());
        cuerpoRespuesta.put("mensaje", "Error interno del servidor");
        cuerpoRespuesta.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(cuerpoRespuesta, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}