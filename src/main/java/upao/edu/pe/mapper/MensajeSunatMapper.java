package upao.edu.pe.mapper;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import upao.edu.pe.dto.MensajeSunatDTO;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.service.SunatServicio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@Slf4j
public class MensajeSunatMapper {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(MensajeSunatMapper.class);

    /**
     * Convierte un DTO a una entidad MensajeSunat
     * @param dto DTO con los datos de origen
     * @return Entidad MensajeSunat
     */
    public MensajeSunat mapearAEntidad(MensajeSunatDTO dto) {
        if (dto == null) {
            return null;
        }

        MensajeSunat entidad = new MensajeSunat();

        entidad.setNuCodigoMensaje(dto.getNuCodigoMensaje());
        entidad.setNuPagina(dto.getNuPagina());
        entidad.setNuEstado(dto.getNuEstado());
        entidad.setNuDestacado(dto.getNuDestacado());
        entidad.setNuUrgente(dto.getNuUrgente());

        // Parsear fecha de vigencia
        entidad.setDtFechaVigencia(parsearFechaVigencia(dto.getDtFechaVigencia()));

        entidad.setNuTipoMensaje(dto.getNuTipoMensaje());
        entidad.setVcAsunto(dto.getVcAsunto());
        entidad.setVcFechaEnvio(dto.getVcFechaEnvio());
        entidad.setVcFechaPublica(dto.getVcFechaPublica());
        entidad.setVcUsuarioEmisor(dto.getVcUsuarioEmisor());
        entidad.setNuIndicadorTexto(dto.getNuIndicadorTexto());
        entidad.setNuTipoGenerador(dto.getNuTipoGenerador());
        entidad.setVcCodigoDependencia(dto.getVcCodigoDependencia());
        entidad.setNuAviso(dto.getNuAviso());
        entidad.setNuCantidadArchivos(dto.getNuCantidadArchivos());
        entidad.setVcCodigoEtiqueta(dto.getVcCodigoEtiqueta());
        entidad.setNuMensaje(dto.getNuMensaje());
        entidad.setVcCodigoCarpeta(dto.getVcCodigoCarpeta());
        entidad.setVcNumeroRuc(dto.getVcNumeroRuc());
        entidad.setClasificacion(dto.getClasificacion());

        return entidad;
    }

    /**
     * Parsea la fecha de vigencia en formato string a LocalDateTime
     * @param fechaStr Fecha en formato string
     * @return LocalDateTime o null si hay error
     */
    LocalDateTime parsearFechaVigencia(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
            return LocalDateTime.parse(fechaStr, formatter);
        } catch (DateTimeParseException e) {
            logger.error("Error al parsear la fecha de vigencia: {}", e.getMessage());
            return null;
        }
    }

    public MensajeSunatDTO mapearADTO(MensajeSunat entidad) {
        if (entidad == null) {
            return null;
        }
        MensajeSunatDTO dto = new MensajeSunatDTO();
        dto.setNuCodigoMensaje(entidad.getNuCodigoMensaje());
        dto.setNuPagina(entidad.getNuPagina());
        dto.setNuEstado(entidad.getNuEstado());
        dto.setNuDestacado(entidad.getNuDestacado());
        dto.setNuUrgente(entidad.getNuUrgente());
        dto.setDtFechaVigencia(entidad.getDtFechaVigencia() != null ? entidad.getDtFechaVigencia().toString() : null);
        dto.setNuTipoMensaje(entidad.getNuTipoMensaje());
        dto.setVcAsunto(entidad.getVcAsunto());
        dto.setVcFechaEnvio(entidad.getVcFechaEnvio());
        dto.setVcFechaPublica(entidad.getVcFechaPublica());
        dto.setVcUsuarioEmisor(entidad.getVcUsuarioEmisor());
        dto.setNuIndicadorTexto(entidad.getNuIndicadorTexto());
        dto.setNuTipoGenerador(entidad.getNuTipoGenerador());
        dto.setVcCodigoDependencia(entidad.getVcCodigoDependencia());
        dto.setNuAviso(entidad.getNuAviso());
        dto.setNuCantidadArchivos(entidad.getNuCantidadArchivos());
        dto.setVcCodigoEtiqueta(entidad.getVcCodigoEtiqueta());
        dto.setNuMensaje(entidad.getNuMensaje());
        dto.setVcCodigoCarpeta(entidad.getVcCodigoCarpeta());
        dto.setVcNumeroRuc(entidad.getVcNumeroRuc());
        dto.setClasificacion(entidad.getClasificacion());
        return dto;
    }
}