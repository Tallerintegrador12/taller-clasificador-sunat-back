package upao.edu.pe.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import upao.edu.pe.dto.NotificacionResponseDto;
import upao.edu.pe.model.DetalleNotificacion;
import upao.edu.pe.service.SunatNotificacionService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SunatNotificacionControllerTest {
    @Mock
    private SunatNotificacionService sunatNotificacionService;
    @InjectMocks
    private SunatNotificacionController controller;

    @Test
    void testObtenerDetalleNotificacion_ok() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        NotificacionResponseDto dto = new NotificacionResponseDto();
        when(sunatNotificacionService.buscarDetallePorCodigo("123")).thenReturn(Optional.of(detalle));
        when(sunatNotificacionService.convertirAFormatoSolicitado(detalle)).thenReturn(dto);
        ResponseEntity<?> response = controller.obtenerDetalleNotificacion("123");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void testObtenerDetalleNotificacion_noEncontrado() {
        when(sunatNotificacionService.buscarDetallePorCodigo("999")).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.obtenerDetalleNotificacion("999");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testObtenerDetalleNotificacion_parametroNulo() {
        ResponseEntity<?> response = controller.obtenerDetalleNotificacion(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testObtenerDetalleNotificacion_parametroVacio() {
        ResponseEntity<?> response = controller.obtenerDetalleNotificacion("");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testObtenerDetalleNotificacion_excepcion() {
        when(sunatNotificacionService.buscarDetallePorCodigo("err")).thenThrow(new RuntimeException("Fallo"));
        ResponseEntity<?> response = controller.obtenerDetalleNotificacion("err");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(String.class);
        assertThat((String)response.getBody()).contains("Fallo");
    }

    @Test
    void testProcesarNotificacion_ok() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        NotificacionResponseDto dto = new NotificacionResponseDto();
        when(sunatNotificacionService.procesarYGuardarNotificacion("123", "cookie")).thenReturn(detalle);
        when(sunatNotificacionService.convertirAFormatoSolicitado(detalle)).thenReturn(dto);
        ResponseEntity<?> response = controller.procesarNotificacion("123", "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void testProcesarNotificacion_parametroNulo() {
        ResponseEntity<?> response = controller.procesarNotificacion(null, "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testProcesarNotificacion_parametroVacio() {
        ResponseEntity<?> response = controller.procesarNotificacion("", "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testProcesarNotificacion_excepcion() {
        when(sunatNotificacionService.procesarYGuardarNotificacion("err", "cookie")).thenThrow(new RuntimeException("Fallo"));
        ResponseEntity<?> response = controller.procesarNotificacion("err", "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(String.class);
        assertThat((String)response.getBody()).contains("Fallo");
    }

    @Test
    void testRefrescarNotificacion_ok() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        NotificacionResponseDto dto = new NotificacionResponseDto();
        when(sunatNotificacionService.buscarDetallePorCodigo("123")).thenReturn(Optional.of(detalle));
        when(sunatNotificacionService.procesarYGuardarNotificacion("123", "cookie")).thenReturn(detalle);
        when(sunatNotificacionService.convertirAFormatoSolicitado(detalle)).thenReturn(dto);
        ResponseEntity<?> response = controller.refrescarNotificacion("123", "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void testRefrescarNotificacion_parametroVacio() {
        ResponseEntity<?> response = controller.refrescarNotificacion("", "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testRefrescarNotificacion_parametroNulo() {
        ResponseEntity<?> response = controller.refrescarNotificacion(null, "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testRefrescarNotificacion_noExisteEnBD() {
        when(sunatNotificacionService.buscarDetallePorCodigo("789")).thenReturn(Optional.empty());
        DetalleNotificacion detalle = new DetalleNotificacion();
        NotificacionResponseDto dto = new NotificacionResponseDto();
        when(sunatNotificacionService.procesarYGuardarNotificacion("789", "cookie")).thenReturn(detalle);
        when(sunatNotificacionService.convertirAFormatoSolicitado(detalle)).thenReturn(dto);
        ResponseEntity<?> response = controller.refrescarNotificacion("789", "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void testObtenerOProcesarNotificacion_encontradoEnBD() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        NotificacionResponseDto dto = new NotificacionResponseDto();
        when(sunatNotificacionService.buscarDetallePorCodigo("123")).thenReturn(Optional.of(detalle));
        when(sunatNotificacionService.convertirAFormatoSolicitado(detalle)).thenReturn(dto);
        ResponseEntity<?> response = controller.obtenerOProcesarNotificacion("123", "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void testObtenerOProcesarNotificacion_noEncontradoProcesa() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        NotificacionResponseDto dto = new NotificacionResponseDto();
        when(sunatNotificacionService.buscarDetallePorCodigo("456")).thenReturn(Optional.empty());
        when(sunatNotificacionService.procesarYGuardarNotificacion("456", "cookie")).thenReturn(detalle);
        when(sunatNotificacionService.convertirAFormatoSolicitado(detalle)).thenReturn(dto);
        ResponseEntity<?> response = controller.obtenerOProcesarNotificacion("456", "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void testObtenerOProcesarNotificacion_parametroNulo() {
        ResponseEntity<?> response = controller.obtenerOProcesarNotificacion(null, "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testObtenerOProcesarNotificacion_excepcion() {
        when(sunatNotificacionService.buscarDetallePorCodigo("err")).thenThrow(new RuntimeException("Fallo"));
        ResponseEntity<?> response = controller.obtenerOProcesarNotificacion("err", "cookie");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(String.class);
        assertThat((String)response.getBody()).contains("Fallo");
    }
}
