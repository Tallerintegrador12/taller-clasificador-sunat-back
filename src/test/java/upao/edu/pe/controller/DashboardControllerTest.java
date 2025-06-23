package upao.edu.pe.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import upao.edu.pe.dto.response.RespuestaControlador;
import upao.edu.pe.service.DashboardService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    void obtenerAlertasCriticas_DeberiaRetornarListaDeAlertas_CuandoSeEjecutaExitosamente() {
        // Arrange
        String ruc = "20123456789";
        List<Map<String, Object>> alertasEsperadas = new ArrayList<>();
        
        Map<String, Object> alerta1 = new HashMap<>();
        alerta1.put("id", "REQUERIMIENTOS_URGENTES");
        alerta1.put("titulo", "Requerimientos SUNAT Urgentes");
        alerta1.put("mensaje", "Tienes 2 requerimientos de SUNAT de los últimos 7 días que requieren respuesta inmediata");
        alerta1.put("tipo", "REQUERIMIENTO");
        alerta1.put("nivelCriticidad", "CRITICA");
        alerta1.put("prioridad", 1);
        alertasEsperadas.add(alerta1);

        when(dashboardService.obtenerAlertasCriticas(ruc)).thenReturn(alertasEsperadas);

        // Act
        ResponseEntity<RespuestaControlador<List<Map<String, Object>>>> resultado = 
            dashboardController.obtenerAlertasCriticas(ruc);        // Assert
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody().getNuCodigo()).isEqualTo(200);
        assertThat(resultado.getBody().getDatos()).isEqualTo(alertasEsperadas);
        assertThat(resultado.getBody().getVcMensaje()).isEqualTo("Alertas críticas obtenidas exitosamente");
        
        verify(dashboardService, times(1)).obtenerAlertasCriticas(ruc);
    }

    @Test
    void obtenerAlertasCriticas_DeberiaRetornarError500_CuandoOcurreExcepcion() {
        // Arrange
        String ruc = "20123456789";
        String mensajeError = "Error simulado en el servicio";
        
        when(dashboardService.obtenerAlertasCriticas(ruc))
            .thenThrow(new RuntimeException(mensajeError));

        // Act
        ResponseEntity<RespuestaControlador<List<Map<String, Object>>>> resultado = 
            dashboardController.obtenerAlertasCriticas(ruc);        // Assert
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody().getNuCodigo()).isEqualTo(500);
        assertThat(resultado.getBody().getDatos()).isNull();
        assertThat(resultado.getBody().getVcMensaje()).contains("Error al obtener alertas críticas");
        
        verify(dashboardService, times(1)).obtenerAlertasCriticas(ruc);
    }

    @Test
    void obtenerAlertasCriticas_DeberiaRetornarListaVacia_CuandoNoHayAlertas() {
        // Arrange
        String ruc = "20123456789";
        List<Map<String, Object>> alertasVacias = new ArrayList<>();
        
        when(dashboardService.obtenerAlertasCriticas(ruc)).thenReturn(alertasVacias);

        // Act
        ResponseEntity<RespuestaControlador<List<Map<String, Object>>>> resultado = 
            dashboardController.obtenerAlertasCriticas(ruc);        // Assert
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody().getNuCodigo()).isEqualTo(200);
        assertThat(resultado.getBody().getDatos()).isEmpty();
        assertThat(resultado.getBody().getVcMensaje()).isEqualTo("Alertas críticas obtenidas exitosamente");
        
        verify(dashboardService, times(1)).obtenerAlertasCriticas(ruc);
    }
}
