package upao.edu.pe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import upao.edu.pe.model.Etiqueta;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtiquetaRepositorio extends JpaRepository<Etiqueta, Long> {

    boolean existsByVcCodigo(String vcCodigo);

    Optional<Etiqueta> findByVcCodigo(String vcCodigo);

    List<Etiqueta> findAllByOrderByNuIdEtiquetaAsc();


    @Query(value = "SELECT COALESCE(MAX(CAST(vc_codigo AS INTEGER)), 16) + 1 FROM t_etiqueta WHERE vc_codigo SIMILAR TO '[0-9]+'", nativeQuery = true)
    Integer obtenerSiguienteCodigoSecuencia();

}