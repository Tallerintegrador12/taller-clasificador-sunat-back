package upao.edu.pe.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.repository.MensajeSunatRepositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensajeSunatServicioTest {
    @Mock
    private MensajeSunatRepositorio mensajeSunatRepositorio;

    @InjectMocks
    private MensajeSunatServicio servicio;

    @Test
    void testInstanciaNoNula() {
        assertThat(servicio).isNotNull();
    }

    @Test
    void testActualizarDestacado_actualizaYGuarda() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setNuCodigoMensaje(1L);
        mensaje.setNuDestacado(0);
        when(repo.findById(1L)).thenReturn(Optional.of(mensaje));
        when(repo.save(mensaje)).thenReturn(mensaje);
        // Usar reflection para inyectar el mock
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        MensajeSunat actualizado = servicio.actualizarDestacado(1L, 1);
        assertThat(actualizado.getNuDestacado()).isEqualTo(1);
        verify(repo).save(mensaje);
    }

    @Test
    void testActualizarUrgente_actualizaYGuarda() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setNuCodigoMensaje(2L);
        mensaje.setNuUrgente(0);
        when(repo.findById(2L)).thenReturn(Optional.of(mensaje));
        when(repo.save(mensaje)).thenReturn(mensaje);
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        MensajeSunat actualizado = servicio.actualizarUrgente(2L, 1);
        assertThat(actualizado.getNuUrgente()).isEqualTo(1);
        verify(repo).save(mensaje);
    }

    @Test
    void testActualizarEstado_actualizaYGuarda() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setNuCodigoMensaje(3L);
        mensaje.setNuEstado(0);
        when(repo.findById(3L)).thenReturn(Optional.of(mensaje));
        when(repo.save(mensaje)).thenReturn(mensaje);
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        MensajeSunat actualizado = servicio.actualizarEstado(3L, 1);
        assertThat(actualizado.getNuEstado()).isEqualTo(1);
        verify(repo).save(mensaje);
    }

    @Test
    void testActualizarEtiqueta_actualizaYGuarda() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setNuCodigoMensaje(4L);
        mensaje.setVcCodigoEtiqueta("00");
        when(repo.findById(4L)).thenReturn(Optional.of(mensaje));
        when(repo.save(mensaje)).thenReturn(mensaje);
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        MensajeSunat actualizado = servicio.actualizarEtiqueta(4L, "10");
        assertThat(actualizado.getVcCodigoEtiqueta()).isEqualTo("10");
        verify(repo).save(mensaje);
    }

    @Test
    void testActualizarLeido_actualizaYGuarda() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setNuCodigoMensaje(5L);
        mensaje.setNuLeido(0);
        when(repo.findById(5L)).thenReturn(Optional.of(mensaje));
        when(repo.save(mensaje)).thenReturn(mensaje);
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        MensajeSunat actualizado = servicio.actualizarLeido(5L, 1);
        assertThat(actualizado.getNuLeido()).isEqualTo(1);
        verify(repo).save(mensaje);
    }

    @Test
    void testActualizarArchivado_actualizaYGuarda() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setNuCodigoMensaje(6L);
        mensaje.setNuArchivado(0);
        when(repo.findById(6L)).thenReturn(Optional.of(mensaje));
        when(repo.save(mensaje)).thenReturn(mensaje);
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        MensajeSunat actualizado = servicio.actualizarArchivado(6L, 1);
        assertThat(actualizado.getNuArchivado()).isEqualTo(1);
        verify(repo).save(mensaje);
    }

    @Test
    void testActualizarDestacado_lanzaExcepcionSiNoExiste() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        when(repo.findById(100L)).thenReturn(Optional.empty());
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> servicio.actualizarDestacado(100L, 1));
    }

    @Test
    void testActualizarUrgente_lanzaExcepcionSiNoExiste() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        when(repo.findById(200L)).thenReturn(Optional.empty());
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> servicio.actualizarUrgente(200L, 1));
    }

    @Test
    void testActualizarEstado_lanzaExcepcionSiNoExiste() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        when(repo.findById(201L)).thenReturn(Optional.empty());
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> servicio.actualizarEstado(201L, 1));
    }

    @Test
    void testActualizarEtiqueta_lanzaExcepcionSiNoExiste() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        when(repo.findById(202L)).thenReturn(Optional.empty());
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> servicio.actualizarEtiqueta(202L, "10"));
    }

    @Test
    void testActualizarLeido_lanzaExcepcionSiNoExiste() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        when(repo.findById(203L)).thenReturn(Optional.empty());
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> servicio.actualizarLeido(203L, 1));
    }

    @Test
    void testActualizarArchivado_lanzaExcepcionSiNoExiste() {
        var repo = mensajeSunatRepositorio;
        var servicio = new MensajeSunatServicio();
        when(repo.findById(204L)).thenReturn(Optional.empty());
        try {
            var field = MensajeSunatServicio.class.getDeclaredField("mensajeSunatRepositorio");
            field.setAccessible(true);
            field.set(servicio, repo);
        } catch (Exception e) { throw new RuntimeException(e); }
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> servicio.actualizarArchivado(204L, 1));
    }

    @Test
    void testObtenerTodosMensajes_devuelveListaEjemplo() {
        String ruc = "12345678901";
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setVcCodigoEtiqueta("00");
        when(mensajeSunatRepositorio.findMensajesOrdenadosPorFecha(ruc)).thenReturn(List.of(mensaje));
        List<MensajeSunat> resultado = servicio.obtenerTodosMensajes(ruc);
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getVcCodigoEtiqueta()).isEqualTo("00");
        verify(mensajeSunatRepositorio).findMensajesOrdenadosPorFecha(ruc);
    }

    @Test
    void testObtenerMensajesPaginados_devuelvePaginaEjemplo() {
        int pagina = 0, cantidad = 2;
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setVcCodigoEtiqueta("00");
        Page<MensajeSunat> page = new org.springframework.data.domain.PageImpl<>(List.of(mensaje));
        when(mensajeSunatRepositorio.encontrarVcCodigoEtiquetaOrderByVcFechaPublicaDesc(eq("00"), any(Pageable.class))).thenReturn(page);
        Page<MensajeSunat> resultado = servicio.obtenerMensajesPaginados(pagina, cantidad);
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getVcCodigoEtiqueta()).isEqualTo("00");
        verify(mensajeSunatRepositorio).encontrarVcCodigoEtiquetaOrderByVcFechaPublicaDesc(eq("00"), any(Pageable.class));
    }

    @Test
    void testObtenerMensajesPorEtiqueta_devuelveListaEjemplo() {
        String etiqueta = "10";
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setVcCodigoEtiqueta(etiqueta);
        when(mensajeSunatRepositorio.findByVcCodigoEtiqueta(etiqueta)).thenReturn(List.of(mensaje));
        List<MensajeSunat> resultado = servicio.obtenerMensajesPorEtiqueta(etiqueta);
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getVcCodigoEtiqueta()).isEqualTo(etiqueta);
        verify(mensajeSunatRepositorio).findByVcCodigoEtiqueta(etiqueta);
    }

    @Test
    void testObtenerMensajesPorEtiquetaPaginados_devuelvePaginaEjemplo() {
        String etiqueta = "10";
        int pagina = 0, cantidad = 2;
        MensajeSunat mensaje = new MensajeSunat();
        mensaje.setVcCodigoEtiqueta(etiqueta);
        Page<MensajeSunat> page = new org.springframework.data.domain.PageImpl<>(List.of(mensaje));
        when(mensajeSunatRepositorio.encontrarVcCodigoEtiqueta(eq(etiqueta), any(Pageable.class))).thenReturn(page);
        Page<MensajeSunat> resultado = servicio.obtenerMensajesPorEtiquetaPaginados(etiqueta, pagina, cantidad);
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getVcCodigoEtiqueta()).isEqualTo(etiqueta);
        verify(mensajeSunatRepositorio).encontrarVcCodigoEtiqueta(eq(etiqueta), any(Pageable.class));
    }

    @Test
    void testObtenerDescripcionEtiqueta_existenteYDesconocida() {
        assertThat(servicio.obtenerDescripcionEtiqueta("10")).isEqualTo("VALORES");
        assertThat(servicio.obtenerDescripcionEtiqueta("99")).isEqualTo("ETIQUETA DESCONOCIDA");
    }

    @Test
    void testObtenerTodasLasEtiquetas_devuelveMapaCompleto() {
        Map<String, String> etiquetas = servicio.obtenerTodasLasEtiquetas();
        assertThat(etiquetas).containsEntry("00", "NO ETIQUETADOS");
        assertThat(etiquetas).containsEntry("10", "VALORES");
        assertThat(etiquetas).containsEntry("16", "AVISOS");
        assertThat(etiquetas).hasSizeGreaterThanOrEqualTo(7);
    }
}
