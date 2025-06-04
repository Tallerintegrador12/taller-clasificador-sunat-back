package upao.edu.pe.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AttachmentDto {

    @JsonProperty("codMensaje")
    private String codMensaje;

    @JsonProperty("codArchivo")
    private Long codArchivo;

    @JsonProperty("nomArchivo")
    private String nomArchivo;

    @JsonProperty("nomAdjunto")
    private String nomAdjunto;

    @JsonProperty("cntTamarch")
    private Long cntTamarch;

    @JsonProperty("numId")
    private Long numId;

    @JsonProperty("indMensaje")
    private String indMensaje;

    @JsonProperty("numEcm")
    private String numEcm;

    @JsonProperty("tamanoArchivoFormat")
    private String tamanoArchivoFormat;
}
