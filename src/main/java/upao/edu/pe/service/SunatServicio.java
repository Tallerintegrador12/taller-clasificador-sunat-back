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


@Service
public class SunatServicio {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(SunatServicio.class);

    private final MensajeSunatRepositorio mensajeSunatRepositorio;
    private final MensajeSunatMapper mensajeSunatMapper;
    private final RestTemplate restTemplate;



    @Value("${sunat.api.url}")
    private String apiUrl;

    @Value("${sunat.api.cookies}")
    private String cookies;

    // Constructor explícito para la inyección de dependencias
    @Autowired
    public SunatServicio(MensajeSunatRepositorio mensajeSunatRepositorio,
                         MensajeSunatMapper mensajeSunatMapper,
                         RestTemplate restTemplate) {
        this.mensajeSunatRepositorio = mensajeSunatRepositorio;
        this.mensajeSunatMapper = mensajeSunatMapper;
        this.restTemplate = restTemplate;
    }

    /*@Scheduled(fixedRate = 360000) // Ejecutar cada minuto*/
    public void SP_CONSULTAR_Y_GUARDAR_MENSAJES() {

        for(int j= 1; j <= 2; j++) {
            for(int i= 1; i<=30; i++){

                logger.info("Iniciando consulta de mensajes SUNAT: {}", LocalDateTime.now());

                HttpHeaders headers = new HttpHeaders();
                headers.set("Cookie", cookies);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                try {
                    ResponseEntity<RespuestaSunatDTO> response = restTemplate.exchange(
                            apiUrl+"/listNotiMenPag?tipoMsj="+j+"&codCarpeta=00&tipoOrden=NADA&page="+i,
                            HttpMethod.GET,
                            entity,
                            RespuestaSunatDTO.class
                    );

                    if (response.getBody() != null && response.getBody().getRows() != null) {
                        List<MensajeSunat> nuevosRegistros = F_PROCESAR_Y_FILTRAR_MENSAJES(response.getBody().getRows());
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



    }

    private List<MensajeSunat> F_PROCESAR_Y_FILTRAR_MENSAJES(List<MensajeSunatDTO> mensajesDTO) {
        List<MensajeSunat> nuevosMensajes = new ArrayList<>();

        for (MensajeSunatDTO dto : mensajesDTO) {
            // Verificar si el mensaje ya existe en la base de datos
            if (!mensajeSunatRepositorio.existsByNuCodigoMensaje(dto.getNuCodigoMensaje())) {
                MensajeSunat mensajeSunat = mensajeSunatMapper.mapearAEntidad(dto);
                nuevosMensajes.add(mensajeSunat);
            }
        }

        return nuevosMensajes;
    }
}