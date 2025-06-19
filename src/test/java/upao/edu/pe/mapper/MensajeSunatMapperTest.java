package upao.edu.pe.mapper;

import org.junit.jupiter.api.Test;
import upao.edu.pe.dto.MensajeSunatDTO;
import upao.edu.pe.model.MensajeSunat;
import static org.assertj.core.api.Assertions.assertThat;

class MensajeSunatMapperTest {
    @Test
    void testMapearAEntidad() {
        MensajeSunatDTO dto = new MensajeSunatDTO();
        dto.setNuCodigoMensaje(1L);
        MensajeSunatMapper mapper = new MensajeSunatMapper();
        MensajeSunat entidad = mapper.mapearAEntidad(dto);
        assertThat(entidad).isNotNull();
        assertThat(entidad.getNuCodigoMensaje()).isEqualTo(1L);
    }
    @Test
    void testMapearAEntidadNull() {
        MensajeSunatMapper mapper = new MensajeSunatMapper();
        assertThat(mapper.mapearAEntidad(null)).isNull();
    }
    @Test
    void testParsearFechaVigencia_valida() {
        MensajeSunatMapper mapper = new MensajeSunatMapper();
        String fecha = "2025-06-17 15:30:45.0";
        assertThat(mapper.parsearFechaVigencia(fecha)).isNotNull();
    }

    @Test
    void testParsearFechaVigencia_nula() {
        MensajeSunatMapper mapper = new MensajeSunatMapper();
        assertThat(mapper.parsearFechaVigencia(null)).isNull();
    }

    @Test
    void testParsearFechaVigencia_vacia() {
        MensajeSunatMapper mapper = new MensajeSunatMapper();
        assertThat(mapper.parsearFechaVigencia("")).isNull();
    }

    @Test
    void testParsearFechaVigencia_invalida() {
        MensajeSunatMapper mapper = new MensajeSunatMapper();
        String fecha = "fecha-invalida";
        assertThat(mapper.parsearFechaVigencia(fecha)).isNull();
    }
}
