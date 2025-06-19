package upao.edu.pe.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MensajeSunatDTOTest {
    @Test
    void testGettersAndSetters() {
        MensajeSunatDTO dto = new MensajeSunatDTO();
        dto.setNuCodigoMensaje(1L);
        dto.setNuPagina(2);
        dto.setNuEstado(1);
        dto.setNuDestacado(1);
        dto.setNuUrgente(0);
        dto.setDtFechaVigencia("2025-06-17 10:00:00.0");
        dto.setNuTipoMensaje(3);
        dto.setVcAsunto("asunto");
        dto.setVcFechaEnvio("2025-06-17");
        dto.setVcFechaPublica("2025-06-17");
        dto.setVcUsuarioEmisor("emisor");
        dto.setNuIndicadorTexto(1);
        assertThat(dto.getNuCodigoMensaje()).isEqualTo(1L);
        assertThat(dto.getNuPagina()).isEqualTo(2);
        assertThat(dto.getNuEstado()).isEqualTo(1);
        assertThat(dto.getNuDestacado()).isEqualTo(1);
        assertThat(dto.getNuUrgente()).isEqualTo(0);
        assertThat(dto.getDtFechaVigencia()).isEqualTo("2025-06-17 10:00:00.0");
        assertThat(dto.getNuTipoMensaje()).isEqualTo(3);
        assertThat(dto.getVcAsunto()).isEqualTo("asunto");
        assertThat(dto.getVcFechaEnvio()).isEqualTo("2025-06-17");
        assertThat(dto.getVcFechaPublica()).isEqualTo("2025-06-17");
        assertThat(dto.getVcUsuarioEmisor()).isEqualTo("emisor");
        assertThat(dto.getNuIndicadorTexto()).isEqualTo(1);
    }

    @Test
    void testSettersExtra() {
        MensajeSunatDTO dto = new MensajeSunatDTO();
        dto.setNuTipoGenerador(5);
        dto.setVcCodigoDependencia("DEP123");
        dto.setNuAviso(1);
        dto.setNuCantidadArchivos(3);
        dto.setVcCodigoEtiqueta("ETQ456");
        dto.setNuMensaje(2);
        dto.setVcCodigoCarpeta("CAR789");
        dto.setVcNumeroRuc("12345678901");
        // Validaciones básicas para asegurar que los setters funcionan
        assertThat(dto.getNuTipoGenerador()).isEqualTo(5);
        assertThat(dto.getVcCodigoDependencia()).isEqualTo("DEP123");
        assertThat(dto.getNuAviso()).isEqualTo(1);
        assertThat(dto.getNuCantidadArchivos()).isEqualTo(3);
        assertThat(dto.getVcCodigoEtiqueta()).isEqualTo("ETQ456");
        assertThat(dto.getNuMensaje()).isEqualTo(2);
        assertThat(dto.getVcCodigoCarpeta()).isEqualTo("CAR789");
        assertThat(dto.getVcNumeroRuc()).isEqualTo("12345678901");
    }
}
