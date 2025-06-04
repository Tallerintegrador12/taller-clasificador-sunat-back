package upao.edu.pe.service;

import ch.qos.logback.classic.Logger;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import upao.edu.pe.model.Etiqueta;
import upao.edu.pe.repository.EtiquetaRepositorio;
import upao.edu.pe.repository.MensajeSunatRepositorio;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EtiquetaServicio {

    private static final Logger log = (Logger) LoggerFactory.getLogger(EtiquetaServicio.class);

    @Autowired
    private EtiquetaRepositorio etiquetaRepositorio;

    @Autowired
    private MensajeSunatRepositorio mensajeSunatRepositorio;

    /**
     * Crea una nueva etiqueta
     * @param vcNombre Nombre de la etiqueta
     * @param vcColor Color de la etiqueta (opcional)
     * @return Etiqueta creada
     */
    public Etiqueta crearEtiqueta(String vcNombre, String vcColor) {
        // Generar el siguiente código secuencial
        log.info("Creando Etiqueta");
        Integer siguienteCodigo = etiquetaRepositorio.obtenerSiguienteCodigoSecuencia();
        log.info("Siguiente codigo: " + siguienteCodigo);
        String codigoGenerado = String.valueOf(siguienteCodigo);
        log.info("Generado: " + codigoGenerado);

        // Verificar que el código no exista (por seguridad)
        while (etiquetaRepositorio.existsByVcCodigo(codigoGenerado)) {
            siguienteCodigo++;
            codigoGenerado = String.valueOf(siguienteCodigo);
        }

        Etiqueta nuevaEtiqueta = Etiqueta.builder()
                .vcNombre(vcNombre)
                .vcColor(vcColor != null ? vcColor : "#007bff") // Color por defecto
                .vcCodigo(codigoGenerado)
                .build();

        return etiquetaRepositorio.save(nuevaEtiqueta);
    }

    /**
     * Crea una nueva etiqueta solo con nombre (color por defecto)
     * @param vcNombre Nombre de la etiqueta
     * @return Etiqueta creada
     */
    public Etiqueta crearEtiqueta(String vcNombre) {
        return crearEtiqueta(vcNombre, null);
    }

    /**
     * Elimina una etiqueta y reasigna todos los mensajes con esa etiqueta a "00" (no etiquetados)
     * @param nuIdEtiqueta ID de la etiqueta a eliminar
     * @return Número de mensajes reasignados
     */
    public int eliminarEtiqueta(Long nuIdEtiqueta) {
        Etiqueta etiqueta = etiquetaRepositorio.findById(nuIdEtiqueta)
                .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada"));

        // Reasignar todos los mensajes con esta etiqueta a "00"
        int mensajesActualizados = mensajeSunatRepositorio.actualizarMensajesANoEtiquetados(etiqueta.getVcCodigo());

        // Eliminar la etiqueta
        etiquetaRepositorio.delete(etiqueta);

        return mensajesActualizados;
    }

    public List<Etiqueta> obtenerTodasLasEtiquetas() {
        return etiquetaRepositorio.findAllByOrderByNuIdEtiquetaAsc();
    }

    /**
     * Obtiene una etiqueta por su ID
     * @param nuIdEtiqueta ID de la etiqueta
     * @return Etiqueta encontrada
     */
    public Etiqueta obtenerEtiquetaPorId(Long nuIdEtiqueta) {
        return etiquetaRepositorio.findById(nuIdEtiqueta)
                .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada"));
    }

    /**
     * Obtiene una etiqueta por su código
     * @param vcCodigo Código de la etiqueta
     * @return Etiqueta encontrada
     */
    public Optional<Etiqueta> obtenerEtiquetaPorCodigo(String vcCodigo) {
        return etiquetaRepositorio.findByVcCodigo(vcCodigo);
    }

    /**
     * Actualiza una etiqueta existente
     * @param nuIdEtiqueta ID de la etiqueta
     * @param vcNombre Nuevo nombre
     * @param vcColor Nuevo color
     * @return Etiqueta actualizada
     */
    public Etiqueta actualizarEtiqueta(Long nuIdEtiqueta, String vcNombre, String vcColor) {
        Etiqueta etiqueta = etiquetaRepositorio.findById(nuIdEtiqueta)
                .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada"));

        if (vcNombre != null && !vcNombre.trim().isEmpty()) {
            etiqueta.setVcNombre(vcNombre);
        }

        if (vcColor != null && !vcColor.trim().isEmpty()) {
            etiqueta.setVcColor(vcColor);
        }

        return etiquetaRepositorio.save(etiqueta);
    }
}
