package upao.edu.pe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    
    @NotBlank(message = "El nombre de usuario es requerido")
    private String username;
    
    @NotBlank(message = "La contraseña es requerida")
    private String password;
    
    @NotBlank(message = "El RUC es requerido")
    @Pattern(regexp = "^\\d{11}$", message = "El RUC debe contener exactamente 11 dígitos")
    private String ruc;
}
