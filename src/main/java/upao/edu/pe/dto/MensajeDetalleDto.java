package upao.edu.pe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MensajeDetalleDto {

    @JsonProperty("sistema")
    private String sistema;

    @JsonProperty("id_archivo")
    private String idArchivo;

    @JsonProperty("dependencia")
    private String dependencia;

    @JsonProperty("num_doc")
    private String numDoc;

    @JsonProperty("cod_mensaje")
    private String codMensaje;

    @JsonProperty("det_mensaje")
    private String detMensaje;

    @JsonProperty("des_tip_doc")
    private String desTipDoc;

    @JsonProperty("id_anexo")
    private String idAnexo;

    @JsonProperty("razon_social")
    private String razonSocial;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("numruc")
    private String numruc;
}
