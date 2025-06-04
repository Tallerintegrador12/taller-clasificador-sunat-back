package upao.edu.pe.dto;

import lombok.Data;

@Data
public class AttachmentResponseDto {

    private String codMensaje;
    private Long codArchivo;
    private String nomArchivo;
    private String nomAdjunto;
    private Long cntTamarch;
    private Long numId;
    private String indMensaje;
    private String numEcm;
    private String tamanoArchivoFormat;
    private String url;
}