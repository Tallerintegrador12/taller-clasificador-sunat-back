package upao.edu.pe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import upao.edu.pe.dto.LoginDTO;
import upao.edu.pe.dto.RegistroDTO;
import upao.edu.pe.exception.UsuarioDuplicadoException;
import upao.edu.pe.model.Usuario;
import upao.edu.pe.repository.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;    public Usuario registrarUsuario(RegistroDTO registroDTO) {
        // Verificar si ya existe un usuario con el mismo nombre de usuario
        if (usuarioRepository.existsByUsername(registroDTO.getUsername())) {
            throw new UsuarioDuplicadoException("El nombre de usuario '" + registroDTO.getUsername() + "' ya está en uso");
        }
        
        // Verificar si ya existe un usuario con el mismo RUC
        if (usuarioRepository.existsByRuc(registroDTO.getRuc())) {
            throw new UsuarioDuplicadoException("El RUC '" + registroDTO.getRuc() + "' ya está registrado");
        }
        
        Usuario usuario = new Usuario();
        usuario.setUsername(registroDTO.getUsername());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        usuario.setRuc(registroDTO.getRuc());
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> login(LoginDTO loginDTO) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(loginDTO.getUsername());
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            if (passwordEncoder.matches(loginDTO.getPassword(), usuario.getPassword()) && loginDTO.getRuc().equals(usuario.getRuc())) {
                return usuarioOptional;
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
}
