package upao.edu.pe.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import upao.edu.pe.dto.MensajeSunatDTO;
import upao.edu.pe.mapper.MensajeSunatMapper;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.repository.MensajeSunatRepositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SunatServicioTest {

    @Mock
    private MensajeSunatRepositorio mensajeSunatRepositorio;
    @Mock
    private MensajeSunatMapper mensajeSunatMapper;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private SunatNotificacionService sunatDetalleService;

    @InjectMocks
    private SunatServicio sunatServicio;

    @Test
    void testInstanciaNoNula() {
        assertThat(sunatServicio).isNotNull();
    }

    @Test
    void testExtraerUsuario_devuelveUsuarioExtraidoCorrecto() {
        String cookie = "12345678901ABC=123;";
        SunatServicio.UsuarioExtraido usuario = sunatServicio.extraerUsuario(cookie);
        assertThat(usuario).isNotNull();
        assertThat(usuario.getRuc()).isEqualTo("12345678901");
        assertThat(usuario.getUsuario()).isEqualTo("ABC");
    }

    @Test
    void testExtraerUsuario_devuelveNullSiNoCoincide() {
        String cookie = "sinformato";
        SunatServicio.UsuarioExtraido usuario = sunatServicio.extraerUsuario(cookie);
        assertThat(usuario).isNull();
    }

    @Test
    void testFProcesarYFiltrarMensajes_agregaSoloNuevos() {
        MensajeSunatDTO dto1 = mock(MensajeSunatDTO.class);
        MensajeSunatDTO dto2 = mock(MensajeSunatDTO.class);
        when(dto1.getNuCodigoMensaje()).thenReturn(1L);
        when(dto2.getNuCodigoMensaje()).thenReturn(2L);
        List<MensajeSunatDTO> dtos = List.of(dto1, dto2);

        when(mensajeSunatRepositorio.existsByNuCodigoMensaje(1L)).thenReturn(false);
        when(mensajeSunatRepositorio.existsByNuCodigoMensaje(2L)).thenReturn(true);
        MensajeSunat entidad1 = new MensajeSunat();
        when(mensajeSunatMapper.mapearAEntidad(dto1)).thenReturn(entidad1);

        // Inyectar el mock de sunatDetalleService manualmente
        try {
            var field = SunatServicio.class.getDeclaredField("sunatDetalleService");
            field.setAccessible(true);
            field.set(sunatServicio, sunatDetalleService);
            var method = SunatServicio.class.getDeclaredMethod("F_PROCESAR_Y_FILTRAR_MENSAJES", List.class, String.class, String.class);
            method.setAccessible(true);
            List<MensajeSunat> result = (List<MensajeSunat>) method.invoke(sunatServicio, dtos, "12345678901", "cookie");
            assertThat(result).containsExactly(entidad1);
            verify(mensajeSunatRepositorio).existsByNuCodigoMensaje(1L);
            verify(mensajeSunatRepositorio).existsByNuCodigoMensaje(2L);
            verify(mensajeSunatMapper).mapearAEntidad(dto1);
            verify(sunatDetalleService, times(2)).procesarYGuardarNotificacion(anyString(), anyString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSP_CONSULTAR_Y_GUARDAR_MENSAJES_exitoYError() {
        // Mocks para simular la respuesta de la API
        String cookie = "12345678901ABC=123;";
        SunatServicio.UsuarioExtraido usuario = new SunatServicio.UsuarioExtraido("12345678901", "ABC");
        // Mock de respuesta de la API
        upao.edu.pe.dto.response.RespuestaSunatDTO respuestaDTO = mock(upao.edu.pe.dto.response.RespuestaSunatDTO.class);
        MensajeSunatDTO dto = mock(MensajeSunatDTO.class);
        when(dto.getNuCodigoMensaje()).thenReturn(1L);
        when(respuestaDTO.getRows()).thenReturn(List.of(dto));
        org.springframework.http.ResponseEntity<upao.edu.pe.dto.response.RespuestaSunatDTO> responseEntity =
                org.springframework.http.ResponseEntity.ok(respuestaDTO);
        when(restTemplate.exchange(anyString(), any(), any(), eq(upao.edu.pe.dto.response.RespuestaSunatDTO.class)))
                .thenReturn(responseEntity)
                .thenThrow(new RuntimeException("API Error")); // Simula error en la segunda llamada
        when(mensajeSunatRepositorio.existsByNuCodigoMensaje(1L)).thenReturn(false);
        when(mensajeSunatMapper.mapearAEntidad(dto)).thenReturn(new MensajeSunat());
        // Inyectar el mock de sunatDetalleService manualmente
        try {
            var field = SunatServicio.class.getDeclaredField("sunatDetalleService");
            field.setAccessible(true);
            field.set(sunatServicio, sunatDetalleService);
        } catch (Exception e) { throw new RuntimeException(e); }
        // Ejecutar método (solo 2 iteraciones para testear éxito y error)
        for (int i = 1; i <= 2; i++) {
            sunatServicio.SP_CONSULTAR_Y_GUARDAR_MENSAJES(cookie);
        }
        verify(restTemplate, atLeastOnce()).exchange(anyString(), any(), any(), eq(upao.edu.pe.dto.response.RespuestaSunatDTO.class));
        verify(mensajeSunatRepositorio, atLeastOnce()).saveAll(anyList());
    }

    @Test
    void testF_PROCESAR_Y_FILTRAR_MENSAJES_listaVacia() throws Exception {
        // Inyectar el mock de sunatDetalleService manualmente
        var field = SunatServicio.class.getDeclaredField("sunatDetalleService");
        field.setAccessible(true);
        field.set(sunatServicio, sunatDetalleService);
        var method = SunatServicio.class.getDeclaredMethod("F_PROCESAR_Y_FILTRAR_MENSAJES", List.class, String.class, String.class);
        method.setAccessible(true);
        List<MensajeSunat> result = (List<MensajeSunat>) method.invoke(sunatServicio, List.of(), "12345678901", "cookie");
        assertThat(result).isEmpty();
    }

    @Test
    void testSP_CONSULTAR_Y_GUARDAR_MENSAJES_usuarioNull() {
        // Si el usuario es null, solo debe loguear y no llamar a la API
        sunatServicio.SP_CONSULTAR_Y_GUARDAR_MENSAJES("sinformato");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testSP_CONSULTAR_Y_GUARDAR_MENSAJES_getRowsNull() {
        // Simula response.getBody() != null pero getRows() == null
        String cookie = "12345678901ABC=123;";
        upao.edu.pe.dto.response.RespuestaSunatDTO respuestaDTO = mock(upao.edu.pe.dto.response.RespuestaSunatDTO.class);
        when(respuestaDTO.getRows()).thenReturn(null);
        org.springframework.http.ResponseEntity<upao.edu.pe.dto.response.RespuestaSunatDTO> responseEntity =
                org.springframework.http.ResponseEntity.ok(respuestaDTO);
        when(restTemplate.exchange(anyString(), any(), any(), eq(upao.edu.pe.dto.response.RespuestaSunatDTO.class)))
                .thenReturn(responseEntity);
        sunatServicio.SP_CONSULTAR_Y_GUARDAR_MENSAJES(cookie);
        // No debe intentar guardar mensajes
        verify(mensajeSunatRepositorio, never()).saveAll(anyList());
    }

    @Test
    void testSP_CONSULTAR_Y_GUARDAR_MENSAJES_getRowsVacio() {
        // Simula response.getBody() != null y getRows() vacío
        String cookie = "12345678901ABC=123;";
        upao.edu.pe.dto.response.RespuestaSunatDTO respuestaDTO = mock(upao.edu.pe.dto.response.RespuestaSunatDTO.class);
        when(respuestaDTO.getRows()).thenReturn(java.util.Collections.emptyList());
        org.springframework.http.ResponseEntity<upao.edu.pe.dto.response.RespuestaSunatDTO> responseEntity =
                org.springframework.http.ResponseEntity.ok(respuestaDTO);
        when(restTemplate.exchange(anyString(), any(), any(), eq(upao.edu.pe.dto.response.RespuestaSunatDTO.class)))
                .thenReturn(responseEntity);
        sunatServicio.SP_CONSULTAR_Y_GUARDAR_MENSAJES(cookie);
        // No debe intentar guardar mensajes
        verify(mensajeSunatRepositorio, never()).saveAll(anyList());
    }

    @Test
    void testSP_CONSULTAR_Y_GUARDAR_MENSAJES_getRowsVacio_cubreElse() {
        // Simula response.getBody() != null y getRows() vacío
        String cookie = "12345678901ABC=123;";
        upao.edu.pe.dto.response.RespuestaSunatDTO respuestaDTO = mock(upao.edu.pe.dto.response.RespuestaSunatDTO.class);
        when(respuestaDTO.getRows()).thenReturn(java.util.Collections.emptyList());
        org.springframework.http.ResponseEntity<upao.edu.pe.dto.response.RespuestaSunatDTO> responseEntity =
                org.springframework.http.ResponseEntity.ok(respuestaDTO);
        when(restTemplate.exchange(anyString(), any(), any(), eq(upao.edu.pe.dto.response.RespuestaSunatDTO.class)))
                .thenReturn(responseEntity);
        sunatServicio.SP_CONSULTAR_Y_GUARDAR_MENSAJES(cookie);
        // No debe intentar guardar mensajes
        verify(mensajeSunatRepositorio, never()).saveAll(anyList());
    }
}
