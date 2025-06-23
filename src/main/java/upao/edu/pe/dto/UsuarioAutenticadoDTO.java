package upao.edu.pe.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class UsuarioAutenticadoDTO {
    private String ruc;
    private String nombreUsuario;
    private String loginTime;

    public UsuarioAutenticadoDTO(String ruc, String nombreUsuario) {
        this.ruc = ruc;
        this.nombreUsuario = nombreUsuario;
        this.loginTime = LocalDateTime.now().toString();
    }
}
