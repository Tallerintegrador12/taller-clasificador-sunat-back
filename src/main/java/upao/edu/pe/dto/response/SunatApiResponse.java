package upao.edu.pe.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import upao.edu.pe.dto.AttachmentDto;

import java.util.List;

@Data
public class SunatApiResponse {

    @JsonProperty("codMensaje")
    private String codMensaje;

    @JsonProperty("indTipmsj")
    private String indTipmsj;

    @JsonProperty("desAsunto")
    private String desAsunto;

    @JsonProperty("msjMensaje")
    private String msjMensaje;

    @JsonProperty("codUsremisor")
    private String codUsremisor;

    @JsonProperty("indTexto")
    private String indTexto;

    @JsonProperty("codUsuario")
    private String codUsuario;

    @JsonProperty("nombUsuario")
    private String nombUsuario;

    @JsonProperty("indEstado")
    private String indEstado;

    @JsonProperty("fecEnvio")
    private String fecEnvio;

    @JsonProperty("fecLectura")
    private String fecLectura;

    @JsonProperty("fecEliminado")
    private String fecEliminado;

    @JsonProperty("fecVigencia")
    private String fecVigencia;

    @JsonProperty("tipoDestino")
    private Integer tipoDestino;

    @JsonProperty("objDestino")
    private String objDestino;

    @JsonProperty("listAttach")
    private List<AttachmentDto> listAttach;

    @JsonProperty("archivoAdjunto")
    private Boolean archivoAdjunto;

    @JsonProperty("url")
    private String url;

}
