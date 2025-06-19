package upao.edu.pe.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upao.edu.pe.model.Etiqueta;
import upao.edu.pe.repository.EtiquetaRepositorio;
import upao.edu.pe.repository.MensajeSunatRepositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EtiquetaServicioTest {
    @Mock
    private EtiquetaRepositorio etiquetaRepositorio;
    @Mock
    private MensajeSunatRepositorio mensajeSunatRepositorio;

    @InjectMocks
    private EtiquetaServicio servicio;

    @Test
    void testInstanciaNoNula() {
        assertThat(servicio).isNotNull();
    }

    @Test
    void testCrearEtiquetaConColor() {
        when(etiquetaRepositorio.obtenerSiguienteCodigoSecuencia()).thenReturn(1);
        when(etiquetaRepositorio.existsByVcCodigo(anyString())).thenReturn(false);
        Etiqueta etiqueta = Etiqueta.builder().vcNombre("Test").vcColor("#fff").vcCodigo("1").build();
        when(etiquetaRepositorio.save(any())).thenReturn(etiqueta);
        Etiqueta result = servicio.crearEtiqueta("Test", "#fff");
        assertThat(result).isNotNull();
        assertThat(result.getVcNombre()).isEqualTo("Test");
    }

    @Test
    void testCrearEtiquetaSoloNombre() {
        when(etiquetaRepositorio.obtenerSiguienteCodigoSecuencia()).thenReturn(2);
        when(etiquetaRepositorio.existsByVcCodigo(anyString())).thenReturn(false);
        Etiqueta etiqueta = Etiqueta.builder().vcNombre("Test2").vcColor("#007bff").vcCodigo("2").build();
        when(etiquetaRepositorio.save(any())).thenReturn(etiqueta);
        Etiqueta result = servicio.crearEtiqueta("Test2");
        assertThat(result.getVcColor()).isEqualTo("#007bff");
    }

    @Test
    void testEliminarEtiqueta() {
        Etiqueta etiqueta = Etiqueta.builder().vcCodigo("3").build();
        when(etiquetaRepositorio.findById(3L)).thenReturn(Optional.of(etiqueta));
        when(mensajeSunatRepositorio.actualizarMensajesANoEtiquetados("3")).thenReturn(5);
        int actualizados = servicio.eliminarEtiqueta(3L);
        assertThat(actualizados).isEqualTo(5);
        verify(etiquetaRepositorio).delete(etiqueta);
    }

    @Test
    void testEliminarEtiquetaNoEncontrada() {
        when(etiquetaRepositorio.findById(99L)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            servicio.eliminarEtiqueta(99L);
        });
    }

    @Test
    void testObtenerTodasLasEtiquetas() {
        List<Etiqueta> lista = List.of(Etiqueta.builder().vcCodigo("1").build());
        when(etiquetaRepositorio.findAllByOrderByNuIdEtiquetaAsc()).thenReturn(lista);
        List<Etiqueta> result = servicio.obtenerTodasLasEtiquetas();
        assertThat(result).hasSize(1);
    }

    @Test
    void testObtenerEtiquetaPorId() {
        Etiqueta etiqueta = Etiqueta.builder().vcCodigo("4").build();
        when(etiquetaRepositorio.findById(4L)).thenReturn(Optional.of(etiqueta));
        Etiqueta result = servicio.obtenerEtiquetaPorId(4L);
        assertThat(result.getVcCodigo()).isEqualTo("4");
    }

    @Test
    void testObtenerEtiquetaPorIdNoEncontrada() {
        when(etiquetaRepositorio.findById(100L)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            servicio.obtenerEtiquetaPorId(100L);
        });
    }

    @Test
    void testObtenerEtiquetaPorCodigo() {
        Etiqueta etiqueta = Etiqueta.builder().vcCodigo("5").build();
        when(etiquetaRepositorio.findByVcCodigo("5")).thenReturn(Optional.of(etiqueta));
        Optional<Etiqueta> result = servicio.obtenerEtiquetaPorCodigo("5");
        assertThat(result).isPresent();
    }

    @Test
    void testActualizarEtiqueta() {
        Etiqueta etiqueta = Etiqueta.builder().vcCodigo("6").vcNombre("Old").vcColor("#000").build();
        when(etiquetaRepositorio.findById(6L)).thenReturn(Optional.of(etiqueta));
        when(etiquetaRepositorio.save(any())).thenReturn(etiqueta);
        Etiqueta result = servicio.actualizarEtiqueta(6L, "Nuevo", "#fff");
        assertThat(result.getVcNombre()).isEqualTo("Nuevo");
        assertThat(result.getVcColor()).isEqualTo("#fff");
    }

    @Test
    void testActualizarEtiquetaNoEncontrada() {
        when(etiquetaRepositorio.findById(101L)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            servicio.actualizarEtiqueta(101L, "nombre", "#fff");
        });
    }

    @Test
    void testActualizarEtiquetaNombreYColorNullOVacio() {
        Etiqueta etiqueta = Etiqueta.builder().vcCodigo("7").vcNombre("Old").vcColor("#000").build();
        when(etiquetaRepositorio.findById(7L)).thenReturn(Optional.of(etiqueta));
        when(etiquetaRepositorio.save(any())).thenReturn(etiqueta);
        Etiqueta result1 = servicio.actualizarEtiqueta(7L, null, null);
        Etiqueta result2 = servicio.actualizarEtiqueta(7L, "   ", "");
        assertThat(result1.getVcNombre()).isEqualTo("Old");
        assertThat(result1.getVcColor()).isEqualTo("#000");
        assertThat(result2.getVcNombre()).isEqualTo("Old");
        assertThat(result2.getVcColor()).isEqualTo("#000");
    }

    @Test
    void testCrearEtiquetaConCodigoExistente() {
        when(etiquetaRepositorio.obtenerSiguienteCodigoSecuencia()).thenReturn(10);
        // Simula que el primer código existe, el segundo no
        when(etiquetaRepositorio.existsByVcCodigo("10")).thenReturn(true);
        when(etiquetaRepositorio.existsByVcCodigo("11")).thenReturn(false);
        Etiqueta etiqueta = Etiqueta.builder().vcNombre("TestLoop").vcColor("#fff").vcCodigo("11").build();
        when(etiquetaRepositorio.save(any())).thenReturn(etiqueta);
        Etiqueta result = servicio.crearEtiqueta("TestLoop", "#fff");
        assertThat(result.getVcCodigo()).isEqualTo("11");
    }
}
