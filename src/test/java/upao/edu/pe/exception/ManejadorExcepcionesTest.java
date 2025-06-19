package upao.edu.pe.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class ManejadorExcepcionesTest {
    @Test
    void testManejarExcepcionCliente() {
        ManejadorExcepciones manejador = new ManejadorExcepciones();
        RestClientException ex = new RestClientException("Error externo");
        ResponseEntity<Map<String, Object>> response = manejador.manejarExcepcionCliente(ex);
        assertThat(response.getStatusCodeValue()).isEqualTo(503);
        assertThat(response.getBody()).containsKey("mensaje");
    }
    @Test
    void testManejarExcepcionGeneral() {
        ManejadorExcepciones manejador = new ManejadorExcepciones();
        Exception ex = new Exception("Error interno");
        ResponseEntity<Map<String, Object>> response = manejador.manejarExcepcionGeneral(ex);
        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).containsKey("mensaje");
    }
}
