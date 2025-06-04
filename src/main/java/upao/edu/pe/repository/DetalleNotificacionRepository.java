package upao.edu.pe.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upao.edu.pe.model.DetalleNotificacion;

import java.util.Optional;

@Repository
public interface DetalleNotificacionRepository extends JpaRepository<DetalleNotificacion, Long> {

    Optional<DetalleNotificacion> findByCodMensaje(String codMensaje);

    @Query("SELECT d FROM DetalleNotificacion d LEFT JOIN FETCH d.listAttach WHERE d.codMensaje = :codMensaje")
    Optional<DetalleNotificacion> findByCodMensajeWithAttachments(@Param("codMensaje") String codMensaje);

    boolean existsByCodMensaje(String codMensaje);
}