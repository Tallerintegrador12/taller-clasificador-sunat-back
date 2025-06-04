package upao.edu.pe.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import upao.edu.pe.model.AttachmentDetalle;

import java.util.List;

@Repository
public interface AttachmentDetalleRepository extends JpaRepository<AttachmentDetalle, Long> {

    List<AttachmentDetalle> findByCodMensaje(String codMensaje);

    void deleteByCodMensaje(String codMensaje);
}
