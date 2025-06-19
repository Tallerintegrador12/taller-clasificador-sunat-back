package upao.edu.pe.dto.response;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RespuestaControladorTest {
    @Test
    void testSettersAndGetters() {
        RespuestaControlador<String> respuesta = new RespuestaControlador<>();
        respuesta.setNuCodigo(404);
        respuesta.setVcErrores(List.of("Error 1", "Error 2"));
        assertThat(respuesta.getNuCodigo()).isEqualTo(404);
        assertThat(respuesta.getVcErrores()).containsExactly("Error 1", "Error 2");
    }

    @Test
    void testGetVcErroresDefaultNull() {
        RespuestaControlador<String> respuesta = new RespuestaControlador<>();
        assertThat(respuesta.getVcErrores()).isNull();
    }

    @Test
    void testGetNuCodigoDefaultNull() {
        RespuestaControlador<String> respuesta = new RespuestaControlador<>();
        assertThat(respuesta.getNuCodigo()).isNull();
    }
}
