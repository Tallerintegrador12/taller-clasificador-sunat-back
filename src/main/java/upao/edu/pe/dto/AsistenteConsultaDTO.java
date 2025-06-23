package upao.edu.pe.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para las consultas al Asistente Virtual Contable
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenteConsultaDTO {
    
    /**
     * La consulta del usuario
     */
    @JsonProperty("consulta")
    private String consulta;
    
    /**
     * ID del usuario para personalizar la respuesta (opcional)
     */
    @JsonProperty("usuarioId")
    private Long usuarioId;
    
    /**
     * Contexto adicional de la consulta (opcional)
     */
    @JsonProperty("contexto")
    private String contexto;
    
    /**
     * Indica si se debe incluir el historial del usuario
     */
    @JsonProperty("incluirHistorial")
    private boolean incluirHistorial = true;
}
