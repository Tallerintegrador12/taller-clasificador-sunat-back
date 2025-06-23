package upao.edu.pe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upao.edu.pe.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByRuc(String ruc);
    boolean existsByUsername(String username);
    boolean existsByRuc(String ruc);
}
