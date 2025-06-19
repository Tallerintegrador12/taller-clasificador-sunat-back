package upao.edu.pe.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import upao.edu.pe.dto.response.RespuestaControlador;
import upao.edu.pe.model.Etiqueta;
import upao.edu.pe.service.EtiquetaServicio;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtiquetaControladorTest {
    @Mock
    private EtiquetaServicio etiquetaServicio;
    @InjectMocks
    private EtiquetaControlador controlador;

    @Test
    void testObtenerTodasLasEtiquetas_devuelveOkYLista() {
        List<Etiqueta> etiquetas = List.of(new Etiqueta());
        when(etiquetaServicio.obtenerTodasLasEtiquetas()).thenReturn(etiquetas);
        ResponseEntity<RespuestaControlador<List<Etiqueta>>> response = controlador.obtenerTodasLasEtiquetas();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).isEqualTo(etiquetas);
    }

    @Test
    void testCrearEtiqueta_devuelveCreado() {
        Etiqueta etiqueta = new Etiqueta();
        when(etiquetaServicio.crearEtiqueta("nombre", null)).thenReturn(etiqueta);
        ResponseEntity<RespuestaControlador<Etiqueta>> response = controlador.crearEtiqueta("nombre", null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).isEqualTo(etiqueta);
    }

    @Test
    void testObtenerEtiquetaPorId_devuelveOk() {
        Etiqueta etiqueta = new Etiqueta();
        when(etiquetaServicio.obtenerEtiquetaPorId(1L)).thenReturn(etiqueta);
        ResponseEntity<RespuestaControlador<Etiqueta>> response = controlador.obtenerEtiquetaPorId(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).isEqualTo(etiqueta);
    }

    @Test
    void testObtenerEtiquetaPorCodigo_devuelveOk() {
        Etiqueta etiqueta = new Etiqueta();
        when(etiquetaServicio.obtenerEtiquetaPorCodigo("A1")).thenReturn(java.util.Optional.of(etiqueta));
        ResponseEntity<RespuestaControlador<Etiqueta>> response = controlador.obtenerEtiquetaPorCodigo("A1");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).isEqualTo(etiqueta);
    }

    @Test
    void testObtenerEtiquetaPorCodigo_noEncontrada() {
        when(etiquetaServicio.obtenerEtiquetaPorCodigo("A2")).thenReturn(java.util.Optional.empty());
        ResponseEntity<RespuestaControlador<Etiqueta>> response = controlador.obtenerEtiquetaPorCodigo("A2");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).isNull();
    }

    @Test
    void testActualizarEtiqueta_devuelveOk() {
        Etiqueta etiqueta = new Etiqueta();
        when(etiquetaServicio.actualizarEtiqueta(1L, "nuevo", "#fff")).thenReturn(etiqueta);
        ResponseEntity<RespuestaControlador<Etiqueta>> response = controlador.actualizarEtiqueta(1L, "nuevo", "#fff");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).isEqualTo(etiqueta);
    }

    @Test
    void testEliminarEtiqueta_devuelveOk() {
        when(etiquetaServicio.eliminarEtiqueta(1L)).thenReturn(5);
        ResponseEntity<RespuestaControlador<java.util.Map<String, Object>>> response = controlador.eliminarEtiqueta(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDatos()).containsEntry("mensajesReasignados", 5);
    }

    @Test
    void testObtenerEtiquetaPorId_lanzaExcepcion() {
        when(etiquetaServicio.obtenerEtiquetaPorId(99L)).thenThrow(new RuntimeException("No existe"));
        try {
            controlador.obtenerEtiquetaPorId(99L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("No existe");
        }
    }
}
