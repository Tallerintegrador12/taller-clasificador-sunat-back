package upao.edu.pe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import upao.edu.pe.dto.response.SunatApiResponse;
import upao.edu.pe.model.DetalleNotificacion;
import upao.edu.pe.repository.AttachmentDetalleRepository;
import upao.edu.pe.repository.DetalleNotificacionRepository;
import upao.edu.pe.dto.MensajeDetalleDto;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class SunatNotificacionServiceTest {
    @Mock
    private DetalleNotificacionRepository detalleNotificacionRepository;
    @Mock
    private AttachmentDetalleRepository attachmentDetalleRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ObjectMapper objectMapper;

    @Spy
    @InjectMocks
    private SunatNotificacionService service;

    @Test
    void testInstanciaNoNula() {
        assertThat(service).isNotNull();
    }

    @Test
    void testBuscarDetallePorCodigo_retornaOptionalCorrecto() {
        String codigo = "12345";
        DetalleNotificacion detalle = new DetalleNotificacion();
        Optional<DetalleNotificacion> esperado = Optional.of(detalle);
        when(detalleNotificacionRepository.findByCodMensajeWithAttachments(codigo)).thenReturn(esperado);

        Optional<DetalleNotificacion> resultado = service.buscarDetallePorCodigo(codigo);

        assertThat(resultado).isPresent();
        assertThat(resultado.get()).isEqualTo(detalle);
        verify(detalleNotificacionRepository).findByCodMensajeWithAttachments(codigo);
    }

    @Test
    void testConvertirAFormatoSolicitado_copiaDatosCorrectamente() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        detalle.setMsjMensaje("mensaje de prueba");
        detalle.setCodUsuario("usuario1");
        detalle.setNombUsuario("nombre usuario");
        // No attachments para este test

        var dto = service.convertirAFormatoSolicitado(detalle);

        assertThat(dto.getMsjMensaje()).isEqualTo("mensaje de prueba");
        assertThat(dto.getCodUsuario()).isEqualTo("usuario1");
        assertThat(dto.getNombUsuario()).isEqualTo("nombre usuario");
        assertThat(dto.getListAttach()).isNull(); // No attachments
    }

    @Test
    void testConsumirApiSunat_lanzaExcepcionEnError() {
        // Arrange
        String codigoMensaje = "codigo";
        String cookie = "cookie";
        // Simular que restTemplate lanza excepción
        when(restTemplate.exchange(anyString(), any(), any(), eq(SunatApiResponse.class)))
                .thenThrow(new RuntimeException("API Error"));
        // Act & Assert
        assertThatThrownBy(() -> service.consumirApiSunat(codigoMensaje, cookie))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al consumir API de SUNAT");
    }

    @Test
    void testProcesarYGuardarNotificacion_existente() {
        String codigo = "123";
        String cookie = "cookie";
        DetalleNotificacion detalle = new DetalleNotificacion();
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(true);
        when(detalleNotificacionRepository.findByCodMensajeWithAttachments(codigo)).thenReturn(Optional.of(detalle));
        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, cookie);
        assertThat(result).isEqualTo(detalle);
    }

    @Test
    void testProcesarYGuardarNotificacion_nuevo() {
        String codigo = "nuevo";
        String cookie = "cookie";
        SunatApiResponse apiResponse = new SunatApiResponse();
        apiResponse.setMsjMensaje("{}" );
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(false);
        Mockito.doReturn(apiResponse).when(service).consumirApiSunat(Mockito.eq(codigo), Mockito.eq(cookie));
        when(detalleNotificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, cookie);
        assertThat(result).isNotNull();
    }

    @Test
    void testEsJsonValido() {
        // Caso válido
        String jsonValido = "{\"key\":\"value\"}";
        boolean resultadoValido = service.esJsonValido(jsonValido);
        assertThat(resultadoValido).isTrue();

        // Caso inválido (no es JSON)
        String jsonInvalido = "no es json";
        boolean resultadoInvalido = service.esJsonValido(jsonInvalido);
        assertThat(resultadoInvalido).isFalse();

        // Caso nulo
        boolean resultadoNulo = service.esJsonValido(null);
        assertThat(resultadoNulo).isFalse();
    }

    @Test
    void testEsJsonValido_parseoFalla() {
        // JSON con llaves pero inválido para el ObjectMapper
        String jsonInvalido = "{mal json}";
        try {
            Mockito.doThrow(new RuntimeException("Parse error")).when(objectMapper).readTree(jsonInvalido);
        } catch (Exception ignored) {}
        boolean resultado = service.esJsonValido(jsonInvalido);
        assertThat(resultado).isFalse();
    }

    @Test
    void testProcesarYGuardarNotificacion_lanzaExcepcionSiNoExiste() {
        String codigo = "noexiste";
        String cookie = "cookie";
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(true);
        when(detalleNotificacionRepository.findByCodMensajeWithAttachments(codigo)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.procesarYGuardarNotificacion(codigo, cookie))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Mensaje no encontrado");
    }

    @Test
    void testConsumirApiSunat_respuestaNoOk() {
        String codigo = "codigo";
        String cookie = "cookie";
        // Simular respuesta no OK
        org.springframework.http.ResponseEntity<SunatApiResponse> response =
                new org.springframework.http.ResponseEntity<>(null, org.springframework.http.HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(), any(), eq(SunatApiResponse.class)))
                .thenReturn(response);
        assertThatThrownBy(() -> service.consumirApiSunat(codigo, cookie))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al consultar API de SUNAT");
    }

    @Test
    void testConsumirApiSunat_respuestaNula() {
        String codigo = "codigo";
        String cookie = "cookie";
        org.springframework.http.ResponseEntity<SunatApiResponse> response =
                new org.springframework.http.ResponseEntity<>(null, org.springframework.http.HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(SunatApiResponse.class)))
                .thenReturn(response);
        assertThatThrownBy(() -> service.consumirApiSunat(codigo, cookie))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al consultar API de SUNAT");
    }

    @Test
    void testConvertirAFormatoSolicitado_conAttachments() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        detalle.setMsjMensaje("mensaje");
        detalle.setCodUsuario("user");
        detalle.setNombUsuario("nombre");
        java.util.List<upao.edu.pe.model.AttachmentDetalle> list = new java.util.ArrayList<>();
        upao.edu.pe.model.AttachmentDetalle att = new upao.edu.pe.model.AttachmentDetalle();
        att.setCodMensaje("c1");
        att.setCodArchivo(1L); // Corregido: Long en vez de String
        att.setNomArchivo("archivo.pdf");
        list.add(att);
        detalle.setListAttach(list);
        var dto = service.convertirAFormatoSolicitado(detalle);
        assertThat(dto.getListAttach()).isNotNull();
        assertThat(dto.getListAttach().get(0).getCodMensaje()).isEqualTo("c1");
    }

    @Test
    void testConvertirAFormatoSolicitado_valoresNulos() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        var dto = service.convertirAFormatoSolicitado(detalle);
        assertThat(dto).isNotNull();
        assertThat(dto.getMsjMensaje()).isNull();
    }

    @Test
    void testDecodificarHtml() {
        String html = "%26%23243;&amp;&lt;&gt;&quot;&#39;";
        String esperado = "ó&<>\"'";
        String resultado = service.decodificarHtml(html);
        assertThat(resultado).isEqualTo(esperado);
        assertThat(service.decodificarHtml(null)).isNull();
    }

    @Test
    void testCodificarHtml() {
        String texto = "ó&<>\"'";
        String esperado = "%26%23243;&amp;&lt;&gt;&quot;&#39;";
        String resultado = service.codificarHtml(texto);
        assertThat(resultado).isEqualTo(esperado);
        assertThat(service.codificarHtml(null)).isNull();
    }

    @Test
    void testProcesarYGuardarNotificacion_mensajeNoJsonValido() {
        String codigo = "nojson";
        String cookie = "cookie";
        SunatApiResponse apiResponse = new SunatApiResponse();
        apiResponse.setMsjMensaje("<html>no json</html>");
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(false);
        Mockito.doReturn(apiResponse).when(service).consumirApiSunat(Mockito.eq(codigo), Mockito.eq(cookie));
        when(detalleNotificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, cookie);
        assertThat(result).isNotNull();
        assertThat(result.getMsjMensaje()).contains("<html>no json</html>");
    }

    @Test
    void testProcesarYGuardarNotificacion_jsonInvalidoLanzaExcepcionAlParsear() throws Exception {
        String codigo = "jsoninvalido";
        String cookie = "cookie";
        SunatApiResponse apiResponse = new SunatApiResponse();
        apiResponse.setMsjMensaje("{mal json}");
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(false);
        Mockito.doReturn(apiResponse).when(service).consumirApiSunat(Mockito.eq(codigo), Mockito.eq(cookie));
        // Simular que esJsonValido devuelve true pero el parseo lanza excepción
        Mockito.doReturn(true).when(service).esJsonValido("{mal json}");
        Mockito.doThrow(new RuntimeException("Parse error")).when(objectMapper).readValue("{mal json}", upao.edu.pe.dto.MensajeDetalleDto.class);
        when(detalleNotificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, cookie);
        assertThat(result).isNotNull();
    }

    @Test
    void testProcesarYGuardarNotificacion_listaAttachmentsVacia() {
        String codigo = "sinattach";
        String cookie = "cookie";
        SunatApiResponse apiResponse = new SunatApiResponse();
        apiResponse.setMsjMensaje("{}");
        apiResponse.setListAttach(new java.util.ArrayList<>()); // vacía
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(false);
        Mockito.doReturn(apiResponse).when(service).consumirApiSunat(Mockito.eq(codigo), Mockito.eq(cookie));
        when(detalleNotificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, cookie);
        assertThat(result).isNotNull();
        assertThat(result.getListAttach()).isNull();
    }

    @Test
    void testProcesarYGuardarNotificacion_lanzaExcepcionGeneral() {
        String codigo = "error";
        String cookie = "cookie";
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.procesarYGuardarNotificacion(codigo, cookie))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al procesar notificación");
    }

    @Test
    void testConvertirAFormatoSolicitado_listaAttachmentsVacia() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        detalle.setMsjMensaje("mensaje");
        detalle.setCodUsuario("user");
        detalle.setNombUsuario("nombre");
        detalle.setListAttach(new java.util.ArrayList<>()); // vacía
        var dto = service.convertirAFormatoSolicitado(detalle);
        assertThat(dto.getListAttach()).isNull();
    }

    @Test
    void testConsumirApiSunat_lanzaExcepcionGeneral() {
        String codigo = "codigo";
        String cookie = "cookie";
        when(restTemplate.exchange(anyString(), any(), any(), eq(SunatApiResponse.class)))
                .thenThrow(new NullPointerException("Null error"));
        assertThatThrownBy(() -> service.consumirApiSunat(codigo, cookie))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al consumir API de SUNAT");
    }

    @Test
    void testProcesarYGuardarNotificacion_attachmentConCamposNulos() {
        String codigo = "casoNulo";
        String cookie = "cookie";
        SunatApiResponse apiResponse = new SunatApiResponse();
        apiResponse.setMsjMensaje("{}" );
        upao.edu.pe.dto.AttachmentDto dto = new upao.edu.pe.dto.AttachmentDto();
        dto.setCodArchivo(null); // Campo nulo
        apiResponse.setListAttach(java.util.List.of(dto));
        apiResponse.setCodUsuario(null); // Campo nulo

        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(false);
        Mockito.doReturn(apiResponse).when(service).consumirApiSunat(Mockito.eq(codigo), Mockito.eq(cookie));
        when(detalleNotificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, cookie);
        assertThat(result).isNotNull();
        assertThat(result.getListAttach()).isNotNull();
        assertThat(result.getListAttach().get(0).getUrl()).isNull(); // No debe generar URL
    }

    @Test
    void testProcesarYGuardarNotificacion_attachmentListaNull() {
        String codigo = "listaNull";
        String cookie = "cookie";
        SunatApiResponse apiResponse = new SunatApiResponse();
        apiResponse.setMsjMensaje("{}");
        apiResponse.setListAttach(null); // lista nula
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(false);
        Mockito.doReturn(apiResponse).when(service).consumirApiSunat(Mockito.eq(codigo), Mockito.eq(cookie));
        when(detalleNotificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, cookie);
        assertThat(result).isNotNull();
        assertThat(result.getListAttach()).isNull();
    }

    @Test
    void testProcesarYGuardarNotificacion_attachmentCodArchivoCero() throws Exception {
        String codigo = "archivoCero";
        String cookie = "cookie";
        SunatApiResponse apiResponse = new SunatApiResponse();
        apiResponse.setMsjMensaje("{}");
        upao.edu.pe.dto.AttachmentDto dto = new upao.edu.pe.dto.AttachmentDto();
        dto.setCodArchivo(0L); // valor límite
        apiResponse.setListAttach(java.util.List.of(dto));
        apiResponse.setCodUsuario("usuario");
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(false);
        Mockito.doReturn(apiResponse).when(service).consumirApiSunat(Mockito.eq(codigo), Mockito.eq(cookie));
        when(detalleNotificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        // Mockear el valor de sunatBaseUrl usando reflexión
        java.lang.reflect.Field field = SunatNotificacionService.class.getDeclaredField("sunatBaseUrl");
        field.setAccessible(true);
        field.set(service, "http://localhost");
        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, cookie);
        assertThat(result).isNotNull();
        assertThat(result.getListAttach()).isNotNull();
        assertThat(result.getListAttach().get(0).getUrl()).isEqualTo("http://localhost/bajarArchivo/0/0/0/usuario.pdf");
    }

    @Test
    void testConsumirApiSunat_respuestaOkYBodyNoNulo() {
        String codigo = "codigo";
        String cookie = "cookie";
        SunatApiResponse apiResponse = new SunatApiResponse();
        org.springframework.http.ResponseEntity<SunatApiResponse> response =
                new org.springframework.http.ResponseEntity<>(apiResponse, org.springframework.http.HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(SunatApiResponse.class)))
                .thenReturn(response);
        SunatApiResponse result = service.consumirApiSunat(codigo, cookie);
        assertThat(result).isNotNull();
    }

    @Test
    void testProcesarYGuardarNotificacion_mensajeDetalleNoNulo() throws Exception {
        String codigo = "conjson";
        String cookie = "cookie";
        SunatApiResponse apiResponse = new SunatApiResponse();
        apiResponse.setMsjMensaje("{\"sistema\":\"sis\",\"idArchivo\":1,\"dependencia\":\"dep\",\"numDoc\":\"123\",\"desTipDoc\":\"doc\",\"idAnexo\":2,\"razonSocial\":\"razon\",\"nombre\":\"nom\",\"numruc\":\"ruc\"}");
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(false);
        Mockito.doReturn(apiResponse).when(service).consumirApiSunat(Mockito.eq(codigo), Mockito.eq(cookie));
        Mockito.doReturn(true).when(service).esJsonValido(anyString());
        MensajeDetalleDto mensajeDetalle = new MensajeDetalleDto();
        mensajeDetalle.setSistema("sis");
        mensajeDetalle.setIdArchivo("1");
        mensajeDetalle.setDependencia("dep");
        mensajeDetalle.setNumDoc("123");
        mensajeDetalle.setDesTipDoc("doc");
        mensajeDetalle.setIdAnexo("2");
        mensajeDetalle.setRazonSocial("razon");
        mensajeDetalle.setNombre("nom");
        mensajeDetalle.setNumruc("ruc");
        Mockito.doReturn(mensajeDetalle).when(objectMapper).readValue(anyString(), eq(MensajeDetalleDto.class));
        when(detalleNotificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, cookie);
        assertThat(result).isNotNull();
        assertThat(result.getSistema()).isEqualTo("sis");
        assertThat(result.getIdArchivo()).isEqualTo("1");
        assertThat(result.getDependencia()).isEqualTo("dep");
        assertThat(result.getNumDoc()).isEqualTo("123");
        assertThat(result.getDesTipDoc()).isEqualTo("doc");
        assertThat(result.getIdAnexo()).isEqualTo("2");
        assertThat(result.getRazonSocial()).isEqualTo("razon");
        assertThat(result.getNombre()).isEqualTo("nom");
        assertThat(result.getNumruc()).isEqualTo("ruc");
    }

    @Test
    void testProcesarYGuardarNotificacion_yaExiste() {
        String codigo = "123";
        DetalleNotificacion detalle = new DetalleNotificacion();
        when(detalleNotificacionRepository.existsByCodMensaje(codigo)).thenReturn(true);
        when(detalleNotificacionRepository.findByCodMensajeWithAttachments(codigo)).thenReturn(Optional.of(detalle));
        DetalleNotificacion result = service.procesarYGuardarNotificacion(codigo, "cookie");
        assertThat(result).isEqualTo(detalle);
        verify(detalleNotificacionRepository).existsByCodMensaje(codigo);
        verify(detalleNotificacionRepository).findByCodMensajeWithAttachments(codigo);
    }

    @Test
    void testEsJsonValido_variosCasos() throws Exception {
        // Caso válido
        String jsonValido = "{\"key\":\"value\"}";
        when(objectMapper.readTree(jsonValido)).thenReturn(new com.fasterxml.jackson.databind.node.ObjectNode(null));
        assertThat(service.esJsonValido(jsonValido)).isTrue();
        // Caso nulo
        assertThat(service.esJsonValido(null)).isFalse();
        // Caso vacío
        assertThat(service.esJsonValido("")).isFalse();
        // Caso no empieza/termina con llaves
        assertThat(service.esJsonValido("[1,2,3]")).isFalse();
        // Caso JSON inválido
        String jsonInvalido = "{key:value}";
        when(objectMapper.readTree(jsonInvalido)).thenThrow(new RuntimeException("error"));
        assertThat(service.esJsonValido(jsonInvalido)).isFalse();
    }
}
