package upao.edu.pe.dto.response;

import org.junit.jupiter.api.Test;
import upao.edu.pe.dto.MensajeSunatDTO;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RespuestaSunatDTOTest {
    @Test
    void testSetAndGetRows() {
        RespuestaSunatDTO dto = new RespuestaSunatDTO();
        MensajeSunatDTO mensaje = new MensajeSunatDTO();
        dto.setRows(List.of(mensaje));
        assertThat(dto.getRows()).hasSize(1);
        assertThat(dto.getRows().get(0)).isSameAs(mensaje);
    }

    @Test
    void testSetAndGetRows_nullAndEmpty() {
        RespuestaSunatDTO dto = new RespuestaSunatDTO();
        dto.setRows(null);
        assertThat(dto.getRows()).isNull();
        dto.setRows(List.of());
        assertThat(dto.getRows()).isEmpty();
    }
}
