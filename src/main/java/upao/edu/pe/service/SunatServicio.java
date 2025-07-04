package upao.edu.pe.service;


import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import upao.edu.pe.dto.MensajeSunatDTO;
import upao.edu.pe.dto.response.RespuestaSunatDTO;
import upao.edu.pe.mapper.MensajeSunatMapper;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.repository.MensajeSunatRepositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class SunatServicio {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(SunatServicio.class);

    private final MensajeSunatRepositorio mensajeSunatRepositorio;
    private final MensajeSunatMapper mensajeSunatMapper;
    private final RestTemplate restTemplate;

    @Autowired
    private SunatNotificacionService sunatDetalleService;

    @Autowired
    private MensajeSunatServicio mensajeSunatServicio;



    @Value("${sunat.api.url}")
    private String apiUrl;

    @Value("${sunat.api.cookies}")
    private String cookies;

    private final Pattern rucUsuarioPattern = Pattern.compile("(\\d{11})([A-Z]+)=\\d*");



    // Constructor explícito para la inyección de dependencias
    @Autowired
    public SunatServicio(MensajeSunatRepositorio mensajeSunatRepositorio,
                         MensajeSunatMapper mensajeSunatMapper,
                         RestTemplate restTemplate) {
        this.mensajeSunatRepositorio = mensajeSunatRepositorio;
        this.mensajeSunatMapper = mensajeSunatMapper;
        this.restTemplate = restTemplate;
    }

    public void SP_CONSULTAR_Y_GUARDAR_MENSAJES(String cookieSunat) {
        UsuarioExtraido usuario = extraerUsuario(cookieSunat);
        if (usuario != null) {
            logger.info("RUC: " + usuario.getRuc() + ", Usuario: " + usuario.getUsuario());
        } else {
            logger.info("No se pudo extraer el RUC ni el usuario.");
            // Si no hay usuario, no se debe llamar a la API ni continuar el proceso
            return;
        }

        for (int i = 1; i <= 30; i++) {
            logger.info("Iniciando consulta de mensajes SUNAT: {}", LocalDateTime.now());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", cookieSunat);  // Usamos la cookie recibida

            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<RespuestaSunatDTO> response = restTemplate.exchange(
                        apiUrl + "/listNotiMenPag?page=" + i,
                        HttpMethod.GET,
                        entity,
                        RespuestaSunatDTO.class
                );

                if (response.getBody() != null && response.getBody().getRows() != null) {
                    assert usuario != null;
                    List<MensajeSunat> nuevosRegistros = F_PROCESAR_Y_FILTRAR_MENSAJES(response.getBody().getRows(), usuario.getRuc(), cookieSunat);
                    if (!nuevosRegistros.isEmpty()) {
                        mensajeSunatRepositorio.saveAll(nuevosRegistros);
                        logger.info("Se han guardado {} nuevos mensajes", nuevosRegistros.size());
                    } else {
                        logger.info("No se encontraron nuevos mensajes para guardar");
                    }
                } else {
                    logger.warn("La respuesta de la API no contiene datos");
                }

            } catch (Exception e) {
                logger.error("Error al consumir la API de SUNAT: {}", e.getMessage(), e);
            }
        }
    }


    private List<MensajeSunat> F_PROCESAR_Y_FILTRAR_MENSAJES(List<MensajeSunatDTO> mensajesDTO, String rucUsuario, String cookieSunat) {
        List<MensajeSunat> nuevosMensajes = new ArrayList<>();

        for (MensajeSunatDTO dto : mensajesDTO) {
            dto.setVcNumeroRuc(rucUsuario);

            // Verificar si el mensaje ya existe en la base de datos
            if (!mensajeSunatRepositorio.existsByNuCodigoMensaje(dto.getNuCodigoMensaje())) {
                MensajeSunat mensajeSunat = mensajeSunatMapper.mapearAEntidad(dto);
                nuevosMensajes.add(mensajeSunat);
            }
            sunatDetalleService.procesarYGuardarNotificacion(dto.getNuCodigoMensaje().toString(),cookieSunat);
        }

        return nuevosMensajes;
    }

    /**
     * Actualiza las clasificaciones de los mensajes existentes usando IA
     */
    public void actualizarClasificacionesExistentes() {
        logger.info("Iniciando actualización de clasificaciones existentes con IA");
        
        // Obtener todos los mensajes sin clasificación o con clasificación anterior
        List<MensajeSunat> mensajes = mensajeSunatRepositorio.findAll();
        
        // Filtrar mensajes que necesitan reclasificación
        List<MensajeSunat> mensajesParaClasificar = mensajes.stream()
            .filter(m -> m.getClasificacion() == null || 
                        m.getClasificacion().equals("SIN CLASIFICACION") ||
                        m.getClasificacion().equals("MUY IMPORTANTE") ||
                        m.getClasificacion().equals("IMPORTANTE") ||
                        m.getClasificacion().equals("RECURRENTE"))
            .collect(java.util.stream.Collectors.toList());
        
        if (mensajesParaClasificar.isEmpty()) {
            logger.info("No se encontraron mensajes que requieran reclasificación con IA");
            return;
        }
        
        logger.info("Procesando {} mensajes para reclasificación con IA", mensajesParaClasificar.size());
        
        // Procesar con IA a través del MensajeSunatServicio
        try {
            mensajeSunatServicio.procesarNuevosCorreosConIA(mensajesParaClasificar);
            logger.info("✅ Reclasificación completada con IA para {} mensajes", mensajesParaClasificar.size());
        } catch (Exception e) {
            logger.error("❌ Error durante la reclasificación con IA: {}", e.getMessage());
        }
    }

    public UsuarioExtraido extraerUsuario(String cookies) {
        Matcher matcher = rucUsuarioPattern.matcher(cookies);
        if (matcher.find()) {
            String ruc = matcher.group(1);
            String usuario = matcher.group(2);
            return new UsuarioExtraido(ruc, usuario);
        }
        return null;
    }

    public static class UsuarioExtraido {
        private String ruc;
        private String usuario;

        public UsuarioExtraido() {
            // Constructor vacío por si se necesita instanciar sin argumentos
        }

        public UsuarioExtraido(String ruc, String usuario) {
            this.ruc = ruc;
            this.usuario = usuario;
        }

        public String getRuc() {
            return ruc;
        }

        public void setRuc(String ruc) {
            this.ruc = ruc;
        }

        public String getUsuario() {
            return usuario;
        }

        public void setUsuario(String usuario) {
            this.usuario = usuario;
        }
    }

}