package upao.edu.pe.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import upao.edu.pe.repository.DetalleNotificacionRepository;

@DataJpaTest
public class DetalleNotificacionTest {

    @Autowired
    private DetalleNotificacionRepository repository;

    @Test
    void prePersist_debeAsignarFechaCreacion() {
        DetalleNotificacion detalle = new DetalleNotificacion();
        // setea los campos obligatorios si existen
        DetalleNotificacion guardado = repository.save(detalle);
        assertThat(guardado.getFechaCreacion()).isNotNull();
        assertThat(guardado.getFechaCreacion()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
