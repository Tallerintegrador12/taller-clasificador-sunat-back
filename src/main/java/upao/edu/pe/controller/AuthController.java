package upao.edu.pe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upao.edu.pe.dto.LoginDTO;
import upao.edu.pe.dto.RegistroDTO;
import upao.edu.pe.dto.UsuarioAutenticadoDTO;
import upao.edu.pe.exception.UsuarioDuplicadoException;
import upao.edu.pe.model.Usuario;
import upao.edu.pe.service.UsuarioService;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "false")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroDTO registroDTO) {
        try {
            Usuario usuario = usuarioService.registrarUsuario(registroDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente");
            response.put("username", usuario.getUsername());
            response.put("ruc", usuario.getRuc());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (UsuarioDuplicadoException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error interno del servidor: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }@PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        System.out.println("=== LOGIN REQUEST ===");
        System.out.println("Username: " + loginDTO.getUsername());
        System.out.println("RUC: " + loginDTO.getRuc());
        System.out.println("Password length: " + (loginDTO.getPassword() != null ? loginDTO.getPassword().length() : "null"));
        
        Optional<Usuario> usuarioOptional = usuarioService.login(loginDTO);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            System.out.println("Login exitoso para usuario: " + usuario.getUsername());
            UsuarioAutenticadoDTO usuarioAutenticado = new UsuarioAutenticadoDTO(
                usuario.getRuc(),
                usuario.getUsername()
            );
            return new ResponseEntity<>(usuarioAutenticado, HttpStatus.OK);
        } else {
            System.out.println("Login fallido - credenciales inválidas");
            return new ResponseEntity<>("Credenciales invalidas", HttpStatus.UNAUTHORIZED);
        }
    }
}
