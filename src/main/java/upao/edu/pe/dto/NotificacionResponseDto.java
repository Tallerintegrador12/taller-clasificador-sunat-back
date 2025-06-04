package upao.edu.pe.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificacionResponseDto {

    private String msjMensaje;
    private String codUsuario;
    private String nombUsuario;
    private List<AttachmentResponseDto> listAttach;
}
