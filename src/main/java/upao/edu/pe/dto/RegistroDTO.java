package upao.edu.pe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDTO {
    
    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 7, max = 15, message = "El nombre de usuario debe tener entre 7 y 15 caracteres")
    @Pattern(regexp = "^[A-Z]+$", message = "El nombre de usuario debe contener solo letras mayúsculas")
    private String username;
      @NotBlank(message = "La contraseña es requerida")
    @Size(min = 5, message = "La contraseña debe tener al menos 5 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9@.]+$", message = "La contraseña solo puede contener letras, números, @ y punto (.)")
    private String password;
    
    @NotBlank(message = "El RUC es requerido")
    @Pattern(regexp = "^\\d{11}$", message = "El RUC debe contener exactamente 11 dígitos")
    private String ruc;
}
