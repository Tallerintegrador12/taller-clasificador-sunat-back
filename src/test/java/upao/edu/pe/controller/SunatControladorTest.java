package upao.edu.pe.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import upao.edu.pe.dto.response.RespuestaControlador;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.service.MensajeSunatServicio;
import upao.edu.pe.service.SunatNotificacionService;
import upao.edu.pe.service.SunatServicio;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SunatControladorTest {
    @Mock
    private SunatServicio sunatServicio;
    @Mock
    private MensajeSunatServicio mensajeSunatServicio;
    @Mock
    private SunatNotificacionService sunatNotificacionService;
    @InjectMocks
    private SunatControlador controlador;

    @Test
    void testSincronizarMensajes_devuelveOk() {
        ResponseEntity<RespuestaControlador<String>> response = controlador.sincronizarMensajes("cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getVcMensaje()).contains("Sincronización iniciada");
        verify(sunatServicio).SP_CONSULTAR_Y_GUARDAR_MENSAJES("cookie");
    }

    @Test
    void testObtenerMensajes_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(1L).build();
        when(mensajeSunatServicio.obtenerTodosMensajes("ruc")).thenReturn(List.of(mensaje));
        ResponseEntity<RespuestaControlador<List<MensajeSunat>>> response = controlador.obtenerMensajes("ruc");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).hasSize(1);
        verify(mensajeSunatServicio).obtenerTodosMensajes("ruc");
    }

    @Test
    void testObtenerMensajesPorEtiqueta_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(2L).build();
        when(mensajeSunatServicio.obtenerMensajesPorEtiqueta("10")).thenReturn(List.of(mensaje));
        when(mensajeSunatServicio.obtenerDescripcionEtiqueta("10")).thenReturn("Etiqueta 10");
        ResponseEntity<RespuestaControlador<List<MensajeSunat>>> response = controlador.obtenerMensajesPorEtiqueta("10");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).hasSize(1);
        assertThat(response.getBody().getVcMensaje()).contains("Etiqueta 10");
    }

    @Test
    void testObtenerEtiquetas_devuelveOk() {
        when(mensajeSunatServicio.obtenerTodasLasEtiquetas()).thenReturn(Map.of("10", "Etiqueta 10"));
        ResponseEntity<RespuestaControlador<Map<String, String>>> response = controlador.obtenerEtiquetas();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).containsKey("10");
    }

    @Test
    void testActualizarDestacado_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(3L).build();
        when(mensajeSunatServicio.actualizarDestacado(3L, 1)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarDestacado(3L, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(3L);
        assertThat(response.getBody().getVcMensaje()).contains("destacado");
    }

    @Test
    void testActualizarDestacado_noDestacado() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(3L).build();
        when(mensajeSunatServicio.actualizarDestacado(3L, 0)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarDestacado(3L, 0);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(3L);
        assertThat(response.getBody().getVcMensaje()).contains("no destacado");
    }

    @Test
    void testActualizarUrgente_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(4L).build();
        when(mensajeSunatServicio.actualizarUrgente(4L, 1)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarUrgente(4L, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(4L);
        assertThat(response.getBody().getVcMensaje()).contains("urgente");
    }

    @Test
    void testActualizarUrgente_noUrgente() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(4L).build();
        when(mensajeSunatServicio.actualizarUrgente(4L, 0)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarUrgente(4L, 0);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(4L);
        assertThat(response.getBody().getVcMensaje()).contains("no urgente");
    }

    @Test
    void testActualizarUrgente_excepcionServicio() {
        when(mensajeSunatServicio.actualizarUrgente(4L, 1)).thenThrow(new RuntimeException("Error"));
        try {
            controlador.actualizarUrgente(4L, 1);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Error");
        }
    }

    @Test
    void testActualizarEstado_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(5L).build();
        when(mensajeSunatServicio.actualizarEstado(5L, 1)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarEstado(5L, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(5L);
        assertThat(response.getBody().getVcMensaje()).contains("activo");
    }

    @Test
    void testActualizarEstado_inactivo() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(5L).build();
        when(mensajeSunatServicio.actualizarEstado(5L, 0)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarEstado(5L, 0);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(5L);
        assertThat(response.getBody().getVcMensaje()).contains("inactivo");
    }

    @Test
    void testActualizarEstado_excepcionServicio() {
        when(mensajeSunatServicio.actualizarEstado(5L, 1)).thenThrow(new RuntimeException("Error"));
        try {
            controlador.actualizarEstado(5L, 1);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Error");
        }
    }

    @Test
    void testActualizarEtiqueta_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(6L).build();
        when(mensajeSunatServicio.actualizarEtiqueta(6L, "10")).thenReturn(mensaje);
        when(mensajeSunatServicio.obtenerDescripcionEtiqueta("10")).thenReturn("Etiqueta 10");
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarEtiqueta(6L, "10");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(6L);
        assertThat(response.getBody().getVcMensaje()).contains("Etiqueta 10");
    }

    @Test
    void testActualizarLeido_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(7L).build();
        when(mensajeSunatServicio.actualizarLeido(7L, 1)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarLeido(7L, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(7L);
        assertThat(response.getBody().getVcMensaje()).contains("leído");
    }

    @Test
    void testActualizarLeido_noLeido() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(7L).build();
        when(mensajeSunatServicio.actualizarLeido(7L, 0)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarLeido(7L, 0);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(7L);
        assertThat(response.getBody().getVcMensaje()).contains("no leído");
    }

    @Test
    void testActualizarLeido_excepcionServicio() {
        when(mensajeSunatServicio.actualizarLeido(7L, 1)).thenThrow(new RuntimeException("Error"));
        try {
            controlador.actualizarLeido(7L, 1);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Error");
        }
    }

    @Test
    void testActualizarArchivado_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(8L).build();
        when(mensajeSunatServicio.actualizarArchivado(8L, 1)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarArchivado(8L, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(8L);
        assertThat(response.getBody().getVcMensaje()).contains("archivado");
    }

    @Test
    void testActualizarArchivado_noArchivado() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(8L).build();
        when(mensajeSunatServicio.actualizarArchivado(8L, 0)).thenReturn(mensaje);
        ResponseEntity<RespuestaControlador<MensajeSunat>> response = controlador.actualizarArchivado(8L, 0);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos().getNuCodigoMensaje()).isEqualTo(8L);
        assertThat(response.getBody().getVcMensaje()).contains("no archivado");
    }

    @Test
    void testActualizarArchivado_excepcionServicio() {
        when(mensajeSunatServicio.actualizarArchivado(8L, 1)).thenThrow(new RuntimeException("Error"));
        try {
            controlador.actualizarArchivado(8L, 1);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Error");
        }
    }

    @Test
    void testObtenerMensajesPorEtiqueta_mensajeSunatServicioLanzaExcepcion() {
        when(mensajeSunatServicio.obtenerMensajesPorEtiqueta("10")).thenThrow(new RuntimeException("Error"));
        try {
            controlador.obtenerMensajesPorEtiqueta("10");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Error");
        }
    }

    @Test
    void testSincronizarMensajes_parametroNulo() {
        try {
            controlador.sincronizarMensajes(null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void testObtenerMensajesPaginados_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(9L).build();
        org.springframework.data.domain.Page<MensajeSunat> page = new org.springframework.data.domain.PageImpl<>(List.of(mensaje), org.springframework.data.domain.PageRequest.of(0, 10), 1);
        when(mensajeSunatServicio.obtenerMensajesPaginados(0, 10)).thenReturn(page);
        ResponseEntity<RespuestaControlador<Map<String, Object>>> response = controlador.obtenerMensajesPaginados(0, 10);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        Map<String, Object> datos = response.getBody().getDatos();
        assertThat(datos).containsKeys("mensajes", "paginaActual", "totalElementos", "totalPaginas");
        assertThat(((List<?>)datos.get("mensajes")).size()).isEqualTo(1);
    }

    @Test
    void testObtenerMensajesPorEtiquetaPaginados_devuelveOk() {
        MensajeSunat mensaje = MensajeSunat.builder().nuCodigoMensaje(10L).build();
        org.springframework.data.domain.Page<MensajeSunat> page = new org.springframework.data.domain.PageImpl<>(List.of(mensaje), org.springframework.data.domain.PageRequest.of(0, 10), 1);
        when(mensajeSunatServicio.obtenerMensajesPorEtiquetaPaginados("10", 0, 10)).thenReturn(page);
        when(mensajeSunatServicio.obtenerDescripcionEtiqueta("10")).thenReturn("Etiqueta 10");
        ResponseEntity<RespuestaControlador<Map<String, Object>>> response = controlador.obtenerMensajesPorEtiquetaPaginados("10", 0, 10);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        Map<String, Object> datos = response.getBody().getDatos();
        assertThat(datos).containsKeys("mensajes", "paginaActual", "totalElementos", "totalPaginas", "etiqueta", "descripcionEtiqueta");
        assertThat(((List<?>)datos.get("mensajes")).size()).isEqualTo(1);
        assertThat(datos.get("descripcionEtiqueta")).isEqualTo("Etiqueta 10");
    }

    @Test
    void testObtenerMensajes_parametroNulo() {
        try {
            controlador.obtenerMensajes(null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void testObtenerMensajes_respuestaVacia() {
        when(mensajeSunatServicio.obtenerTodosMensajes("ruc")).thenReturn(List.of());
        ResponseEntity<RespuestaControlador<List<MensajeSunat>>> response = controlador.obtenerMensajes("ruc");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).isEmpty();
    }

    @Test
    void testObtenerMensajes_excepcionServicio() {
        when(mensajeSunatServicio.obtenerTodosMensajes("ruc")).thenThrow(new RuntimeException("Error"));
        try {
            controlador.obtenerMensajes("ruc");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Error");
        }
    }

    @Test
    void testObtenerMensajes_listaGrande() {
        List<MensajeSunat> lista = java.util.stream.IntStream.range(0, 1000)
                .mapToObj(i -> MensajeSunat.builder().nuCodigoMensaje((long) i).build())
                .toList();
        when(mensajeSunatServicio.obtenerTodosMensajes("ruc")).thenReturn(lista);
        ResponseEntity<RespuestaControlador<List<MensajeSunat>>> response = controlador.obtenerMensajes("ruc");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).hasSize(1000);
    }

    @Test
    void testObtenerMensajesPaginados_paginaNegativa() {
        org.springframework.data.domain.Page<MensajeSunat> page = new org.springframework.data.domain.PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(0, 10), 0);
        when(mensajeSunatServicio.obtenerMensajesPaginados(-1, 10)).thenReturn(page);
        ResponseEntity<RespuestaControlador<Map<String, Object>>> response = controlador.obtenerMensajesPaginados(-1, 10);
        assertThat(response.getBody()).isNotNull();
        assertThat(((List<?>)response.getBody().getDatos().get("mensajes")).size()).isEqualTo(0);
    }

    @Test
    void testObtenerMensajesPaginados_paginaMuyAlta() {
        org.springframework.data.domain.Page<MensajeSunat> page = new org.springframework.data.domain.PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(999, 10), 0);
        when(mensajeSunatServicio.obtenerMensajesPaginados(999, 10)).thenReturn(page);
        ResponseEntity<RespuestaControlador<Map<String, Object>>> response = controlador.obtenerMensajesPaginados(999, 10);
        assertThat(response.getBody()).isNotNull();
        assertThat(((List<?>)response.getBody().getDatos().get("mensajes")).size()).isEqualTo(0);
    }

    @Test
    void testActualizarDestacado_excepcionServicio() {
        when(mensajeSunatServicio.actualizarDestacado(3L, 1)).thenThrow(new RuntimeException("Error"));
        try {
            controlador.actualizarDestacado(3L, 1);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Error");
        }
    }
}
