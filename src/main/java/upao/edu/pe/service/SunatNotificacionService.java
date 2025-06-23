package upao.edu.pe.service;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import upao.edu.pe.dto.AttachmentDto;
import upao.edu.pe.dto.AttachmentResponseDto;
import upao.edu.pe.dto.MensajeDetalleDto;
import upao.edu.pe.dto.NotificacionResponseDto;
import upao.edu.pe.dto.response.SunatApiResponse;
import upao.edu.pe.model.AttachmentDetalle;
import upao.edu.pe.model.DetalleNotificacion;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.repository.AttachmentDetalleRepository;
import upao.edu.pe.repository.DetalleNotificacionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SunatNotificacionService {
    private static final Logger log = (Logger) LoggerFactory.getLogger(SunatNotificacionService.class);

    private final DetalleNotificacionRepository detalleNotificacionRepository;
    private final AttachmentDetalleRepository attachmentDetalleRepository;
    private final MensajeSunatServicio mensajeSunatServicio;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sunat.api.url}")
    private String sunatBaseUrl;

    /*@Value("${sunat.api.cookies}")
    private String sunatCookie;
*/
    public SunatNotificacionService(DetalleNotificacionRepository detalleNotificacionRepository, 
                                   AttachmentDetalleRepository attachmentDetalleRepository, 
                                   MensajeSunatServicio mensajeSunatServicio,
                                   RestTemplate restTemplate, 
                                   ObjectMapper objectMapper) {
        this.detalleNotificacionRepository = detalleNotificacionRepository;
        this.attachmentDetalleRepository = attachmentDetalleRepository;
        this.mensajeSunatServicio = mensajeSunatServicio;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Consume la API de SUNAT para obtener el detalle de una notificación
     */
    public SunatApiResponse consumirApiSunat(String codigoMensaje, String sunatCookie) {
        try {
            String url = sunatBaseUrl + "/obtenerDetalleNotiMen" +
                    "?codigoMensaje=" + codigoMensaje + "&tipoMsj=2";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", sunatCookie);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("Consultando API SUNAT para código de mensaje: {}", codigoMensaje);

            ResponseEntity<SunatApiResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, SunatApiResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Respuesta exitosa de API SUNAT para código: {}", codigoMensaje);
                log.info("Respuesta: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Error en respuesta de API SUNAT. Status: {}", response.getStatusCode());
                throw new RuntimeException("Error al consultar API de SUNAT");
            }

        } catch (Exception e) {
            log.error("Error al consumir API de SUNAT para código {}: {}", codigoMensaje, e.getMessage());
            throw new RuntimeException("Error al consumir API de SUNAT: " + e.getMessage());
        }
    }

    /**
     * Procesa y guarda los detalles del mensaje en la base de datos
     */
    @Transactional
    public DetalleNotificacion procesarYGuardarNotificacion(String codigoMensaje, String cookie) {
        try {
            // Verificar si ya existe el mensaje
            if (detalleNotificacionRepository.existsByCodMensaje(codigoMensaje)) {
                log.info("El mensaje {} ya existe en la base de datos", codigoMensaje);
                return detalleNotificacionRepository.findByCodMensajeWithAttachments(codigoMensaje)
                        .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
            }

            // Consumir API de SUNAT
            SunatApiResponse apiResponse = consumirApiSunat(codigoMensaje, cookie);

            // Parsear el JSON del mensaje solo si es JSON válido
            MensajeDetalleDto mensajeDetalle = null;
            if (apiResponse.getMsjMensaje() != null && !apiResponse.getMsjMensaje().isEmpty()) {

                // Validar si el contenido es JSON antes de parsearlo
                if (esJsonValido(apiResponse.getMsjMensaje())) {
                    try {
                        mensajeDetalle = objectMapper.readValue(apiResponse.getMsjMensaje(), MensajeDetalleDto.class);
                        log.info("Mensaje parseado como JSON: {}", mensajeDetalle);
                    } catch (Exception e) {
                        log.warn("Error al parsear JSON, se tratará como mensaje de texto: {}", e.getMessage());
                        mensajeDetalle = null;
                    }
                } else {
                    log.info("El mensaje no es JSON válido, se tratará como contenido HTML/texto");
                }
            }

            // Crear y guardar DetalleNotificacion
            DetalleNotificacion detalleNotificacion = new DetalleNotificacion();
            detalleNotificacion.setCodMensaje(codigoMensaje);
            detalleNotificacion.setMsjMensaje(apiResponse.getMsjMensaje().toString());
            detalleNotificacion.setCodUsuario(apiResponse.getCodUsuario());
            detalleNotificacion.setNombUsuario(apiResponse.getNombUsuario());
            detalleNotificacion.setUrl(apiResponse.getUrl());


            // Solo asignar valores del DTO si se pudo parsear correctamente
            if (mensajeDetalle != null) {
                detalleNotificacion.setSistema(mensajeDetalle.getSistema());
                detalleNotificacion.setIdArchivo(mensajeDetalle.getIdArchivo());
                detalleNotificacion.setDependencia(mensajeDetalle.getDependencia());
                detalleNotificacion.setNumDoc(mensajeDetalle.getNumDoc());
                detalleNotificacion.setDesTipDoc(decodificarHtml(mensajeDetalle.getDesTipDoc()));
                detalleNotificacion.setIdAnexo(mensajeDetalle.getIdAnexo());
                detalleNotificacion.setRazonSocial(mensajeDetalle.getRazonSocial());
                detalleNotificacion.setNombre(mensajeDetalle.getNombre());
                detalleNotificacion.setNumruc(mensajeDetalle.getNumruc());
            } else {
                // Si no es JSON, se pueden asignar valores por defecto o dejar nulos
                log.info("No se pudieron extraer datos estructurados del mensaje");
            }

            detalleNotificacion = detalleNotificacionRepository.save(detalleNotificacion);



            // Procesar y guardar attachments
            if (apiResponse.getListAttach() != null && !apiResponse.getListAttach().isEmpty()) {
                List<AttachmentDetalle> attachments = new ArrayList<>();

                for (AttachmentDto attachDto : apiResponse.getListAttach()) {
                    AttachmentDetalle attachment = new AttachmentDetalle();
                    attachment.setCodMensaje(codigoMensaje);
                    attachment.setCodArchivo(attachDto.getCodArchivo());
                    attachment.setNomArchivo(attachDto.getNomArchivo());
                    attachment.setNomAdjunto(attachDto.getNomAdjunto());
                    attachment.setCntTamarch(attachDto.getCntTamarch());
                    attachment.setNumId(attachDto.getNumId());
                    attachment.setIndMensaje(attachDto.getIndMensaje());
                    attachment.setNumEcm(attachDto.getNumEcm());
                    attachment.setTamanoArchivoFormat(attachDto.getTamanoArchivoFormat());
                    attachment.setDetalleNotificacion(detalleNotificacion);

                    // Generar URL de descarga
                    if (attachDto.getCodArchivo() != null && apiResponse.getCodUsuario() != null) {
                        String downloadUrl = sunatBaseUrl + "/bajarArchivo/" +
                                attachDto.getCodArchivo() + "/0/0/" + apiResponse.getCodUsuario() + ".pdf";
                        attachment.setUrl(downloadUrl);
                    }

                    attachments.add(attachment);
                }

                attachmentDetalleRepository.saveAll(attachments);
                detalleNotificacion.setListAttach(attachments);
            }

            log.info("Notificación {} procesada y guardada exitosamente", codigoMensaje);
            
            // 🤖 INTEGRACIÓN CON GEMINI AI - Procesar correo nuevo
            try {
                log.info("🤖 Iniciando procesamiento con Gemini AI para correo: {}", codigoMensaje);
                
                // Crear MensajeSunat desde DetalleNotificacion para Gemini AI
                MensajeSunat mensajeSunat = crearMensajeSunatDesdeDetalle(detalleNotificacion, apiResponse);
                
                // Procesar con Gemini AI
                MensajeSunat mensajeProcesado = mensajeSunatServicio.procesarCorreoIndividualConIA(mensajeSunat);
                
                log.info("✅ Correo {} procesado con Gemini AI - Clasificación: {} - Etiqueta: {}", 
                    codigoMensaje, 
                    mensajeProcesado.getClasificacion(),
                    mensajeProcesado.getVcCodigoEtiqueta());
                
            } catch (Exception e) {
                log.error("❌ Error al procesar correo {} con Gemini AI: {}", codigoMensaje, e.getMessage());
                // No fallar la sincronización completa por errores de Gemini
            }
            
            return detalleNotificacion;

        } catch (Exception e) {
            log.error("Error al procesar notificación {}: {}", codigoMensaje, e.getMessage());
            throw new RuntimeException("Error al procesar notificación: " + e.getMessage());
        }
    }

    /**
     * Valida si una cadena es JSON válido
     * @param jsonString La cadena a validar
     * @return true si es JSON válido, false en caso contrario
     */
    boolean esJsonValido(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        // Verificar si empieza y termina con llaves (JSON object)
        String trimmed = jsonString.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return false;
        }

        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Busca el detalle de un mensaje por código
     */
    public Optional<DetalleNotificacion> buscarDetallePorCodigo(String codigoMensaje) {
        return detalleNotificacionRepository.findByCodMensajeWithAttachments(codigoMensaje);
    }

    /**
     * Convierte la respuesta al formato solicitado
     */
    public NotificacionResponseDto convertirAFormatoSolicitado(DetalleNotificacion detalle) {
        NotificacionResponseDto response = new NotificacionResponseDto();

        // Construir el JSON del mensaje
        if (detalle.getMsjMensaje()!= null) {
            response.setMsjMensaje(detalle.getMsjMensaje());
        }

        response.setCodUsuario(detalle.getCodUsuario());
        response.setNombUsuario(detalle.getNombUsuario());

        // Convertir attachments
        if (detalle.getListAttach() != null) {
            if (detalle.getListAttach().isEmpty()) {
                response.setListAttach(null);
            } else {
                List<AttachmentResponseDto> attachments = new ArrayList<>();
                for (AttachmentDetalle attach : detalle.getListAttach()) {
                    AttachmentResponseDto attachDto = new AttachmentResponseDto();
                    attachDto.setCodMensaje(attach.getCodMensaje());
                    attachDto.setCodArchivo(attach.getCodArchivo());
                    attachDto.setNomArchivo(attach.getNomArchivo());
                    attachDto.setNomAdjunto(attach.getNomAdjunto());
                    attachDto.setCntTamarch(attach.getCntTamarch());
                    attachDto.setNumId(attach.getNumId());
                    attachDto.setIndMensaje(attach.getIndMensaje());
                    attachDto.setNumEcm(attach.getNumEcm());
                    attachDto.setTamanoArchivoFormat(attach.getTamanoArchivoFormat());
                    attachDto.setUrl(attach.getUrl());
                    attachments.add(attachDto);
                }
                response.setListAttach(attachments);
            }
        }

        return response;
    }

    public String decodificarHtml(String texto) {
        if (texto == null) return null;
        return texto.replace("%26%23243;", "ó")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

    public String codificarHtml(String texto) {
        if (texto == null) return null;
        return texto.replace("ó", "%26%23243;")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Crea un MensajeSunat desde un DetalleNotificacion para procesamiento con Gemini AI
     */
    private MensajeSunat crearMensajeSunatDesdeDetalle(DetalleNotificacion detalle, SunatApiResponse apiResponse) {
        MensajeSunat mensaje = new MensajeSunat();
        
        // Mapear campos básicos
        mensaje.setNuCodigoMensaje(Long.parseLong(detalle.getCodMensaje()));
        mensaje.setVcAsunto(detalle.getCodMensaje()); // Usar código como asunto temporal
        mensaje.setVcUsuarioEmisor("SUNAT");
        
        // ARREGLO: Asegurar que el RUC se asigne correctamente
        String rucFinal = null;
        if (detalle.getNumruc() != null && !detalle.getNumruc().trim().isEmpty()) {
            rucFinal = detalle.getNumruc().trim();
        } else if (apiResponse.getCodUsuario() != null && !apiResponse.getCodUsuario().trim().isEmpty()) {
            rucFinal = apiResponse.getCodUsuario().trim();
        }
        
        mensaje.setVcNumeroRuc(rucFinal);
        mensaje.setVcCodigoEtiqueta("00"); // Inicialmente sin etiqueta
        mensaje.setNuLeido(0);
        mensaje.setNuArchivado(0);
        
        // Mapear fechas si están disponibles
        if (apiResponse.getFecEnvio() != null) {
            mensaje.setVcFechaEnvio(apiResponse.getFecEnvio());
        }
        
        // Si hay información adicional, construir un asunto más descriptivo
        StringBuilder asuntoBuilder = new StringBuilder();
        
        if (detalle.getDesTipDoc() != null && !detalle.getDesTipDoc().isEmpty()) {
            asuntoBuilder.append(detalle.getDesTipDoc());
        } else if (detalle.getRazonSocial() != null) {
            asuntoBuilder.append("Notificación para ").append(detalle.getRazonSocial());
        } else {
            asuntoBuilder.append("Notificación SUNAT - Código: ").append(detalle.getCodMensaje());
        }
        
        // Si hay información del mensaje, agregarla
        if (detalle.getMsjMensaje() != null && !detalle.getMsjMensaje().isEmpty()) {
            // Extraer información relevante del mensaje
            String mensajeTexto = detalle.getMsjMensaje();
            
            // Buscar patrones comunes en los mensajes
            if (mensajeTexto.contains("multa")) {
                asuntoBuilder.append(" - Multa Tributaria");
            } else if (mensajeTexto.contains("cobranza")) {
                asuntoBuilder.append(" - Resolución de Cobranza");
            } else if (mensajeTexto.contains("fiscalización")) {
                asuntoBuilder.append(" - Fiscalización");
            } else if (mensajeTexto.contains("valores")) {
                asuntoBuilder.append(" - Valores");
            } else if (mensajeTexto.contains("registros")) {
                asuntoBuilder.append(" - Registros Electrónicos");
            }
        }
        
        mensaje.setVcAsunto(asuntoBuilder.toString());
        
        log.info("📧 Creado MensajeSunat para Gemini AI: ID={}, Asunto='{}', RUC={}", 
            mensaje.getNuCodigoMensaje(), 
            mensaje.getVcAsunto(), 
            mensaje.getVcNumeroRuc());
        
        return mensaje;
    }
}
