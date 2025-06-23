package upao.edu.pe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upao.edu.pe.model.MensajeSunat;

import java.util.List;


@Repository
public interface MensajeSunatRepositorio extends JpaRepository<MensajeSunat, Long> {
    boolean existsByNuCodigoMensaje(Long nuCodigoMensaje);

    @Query(value = """
    SELECT * 
    FROM t_mensaje_sunat 
    WHERE vc_numero_ruc = :vc_numero_ruc
    ORDER BY TO_TIMESTAMP(vc_fecha_publica, 'DD/MM/YYYY HH24:MI:SS') DESC
    """, nativeQuery = true)
    List<MensajeSunat> findMensajesOrdenadosPorFecha(String vc_numero_ruc);

    // Método para obtener mensajes paginados ordenados por fecha de publicación con etiqueta "00"
    @Query(value = """
    SELECT * 
    FROM t_mensaje_sunat 
    WHERE vc_codigo_etiqueta = :vcCodigoEtiqueta 
    ORDER BY TO_TIMESTAMP(vc_fecha_publica, 'DD/MM/YYYY HH24:MI:SS') DESC
    """, nativeQuery = true)
    Page<MensajeSunat> encontrarVcCodigoEtiquetaOrderByVcFechaPublicaDesc(String vcCodigoEtiqueta, Pageable pageable);

    // Métodos para buscar por etiqueta específica
    List<MensajeSunat> findByVcCodigoEtiqueta(String vcCodigoEtiqueta);

    // Método para buscar por etiqueta con paginación
    @Query(value = """
    SELECT * 
    FROM t_mensaje_sunat 
    WHERE vc_codigo_etiqueta = :vcCodigoEtiqueta 
    ORDER BY TO_TIMESTAMP(vc_fecha_publica, 'DD/MM/YYYY HH24:MI:SS') DESC
    """, nativeQuery = true)
    Page<MensajeSunat> encontrarVcCodigoEtiqueta(String vcCodigoEtiqueta, Pageable pageable);

    // Método para actualizar mensajes que tienen una etiqueta específica a "00"
    @Modifying
    @Query("UPDATE MensajeSunat m SET m.vcCodigoEtiqueta = '00' WHERE m.vcCodigoEtiqueta = :codigoEtiqueta")
    int actualizarMensajesANoEtiquetados(@Param("codigoEtiqueta") String codigoEtiqueta);

    // Métodos para obtener estadísticas
    long countByVcCodigoEtiqueta(String vcCodigoEtiqueta);
    long countByNuDestacado(Integer nuDestacado);
    long countByNuUrgente(Integer nuUrgente);
}
