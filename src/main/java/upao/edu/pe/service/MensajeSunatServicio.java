package upao.edu.pe.service;

import ch.qos.logback.classic.Logger;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.repository.MensajeSunatRepositorio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MensajeSunatServicio {

    private static final Logger log = (Logger) LoggerFactory.getLogger(MensajeSunatServicio.class);

    @Autowired
    private MensajeSunatRepositorio mensajeSunatRepositorio;

    private static final Map<String, String> ETIQUETAS = new HashMap<>();

    static {
        ETIQUETAS.put("00", "NO ETIQUETADOS");
        ETIQUETAS.put("10", "VALORES");
        ETIQUETAS.put("11", "RESOLUCIONES DE COBRANZAS");
        ETIQUETAS.put("13", "RESOLUCIONES NO CONTENCIOSAS");
        ETIQUETAS.put("14", "RESOLUCIONES DE FISCALIZACION");
        ETIQUETAS.put("15", "RESOLUCIONES ANTERIORES");
        ETIQUETAS.put("16", "AVISOS");
    }

    /**
     * Obtiene todos los mensajes ordenados por fecha de publicación con etiqueta "00"
     */
    public List<MensajeSunat> obtenerTodosMensajes() {
        return mensajeSunatRepositorio.findMensajesOrdenadosPorFecha();
    }

    /**
     * Obtiene mensajes paginados con etiqueta "00"
     * @param pagina Número de página (comenzando desde 0)
     * @param cantidad Cantidad de registros por página
     * @return Página de mensajes
     */
    public Page<MensajeSunat> obtenerMensajesPaginados(int pagina, int cantidad) {
        Pageable pageable = PageRequest.of(pagina, cantidad);
        return mensajeSunatRepositorio.encontrarVcCodigoEtiquetaOrderByVcFechaPublicaDesc("00", pageable);
    }

    /**
     * Obtiene mensajes filtrados por código de etiqueta
     * @param codigoEtiqueta Código de la etiqueta a filtrar (10, 11, 13, 14, 15, 16)
     * @return Lista de mensajes que corresponden a la etiqueta
     */
    public List<MensajeSunat> obtenerMensajesPorEtiqueta(String codigoEtiqueta) {
        return mensajeSunatRepositorio.findByVcCodigoEtiqueta(codigoEtiqueta);
    }

    /**
     * Obtiene mensajes paginados filtrados por código de etiqueta
     * @param codigoEtiqueta Código de la etiqueta a filtrar
     * @param pagina Número de página (comenzando desde 0)
     * @param cantidad Cantidad de registros por página
     * @return Página de mensajes que corresponden a la etiqueta
     */
    public Page<MensajeSunat> obtenerMensajesPorEtiquetaPaginados(String codigoEtiqueta, int pagina, int cantidad) {
        Pageable pageable = PageRequest.of(pagina, cantidad);
        return mensajeSunatRepositorio.encontrarVcCodigoEtiqueta(codigoEtiqueta, pageable);
    }

    /**
     * Obtiene el nombre descriptivo de una etiqueta
     * @param codigoEtiqueta Código de la etiqueta
     * @return Nombre descriptivo de la etiqueta
     */
    public String obtenerDescripcionEtiqueta(String codigoEtiqueta) {
        return ETIQUETAS.getOrDefault(codigoEtiqueta, "ETIQUETA DESCONOCIDA");
    }

    /**
     * Obtiene un mapa de todas las etiquetas disponibles
     * @return Mapa con códigos y descripciones de etiquetas
     */
    public Map<String, String> obtenerTodasLasEtiquetas() {
        return ETIQUETAS;
    }

    // ========== NUEVAS FUNCIONES ==========

    /**
     * Actualiza el estado destacado de un mensaje
     * @param nuCodigoMensaje ID del mensaje
     * @param destacado 1: destacado, 0: no destacado
     * @return Mensaje actualizado
     */
    public MensajeSunat actualizarDestacado(Long nuCodigoMensaje, Integer destacado) {
        MensajeSunat mensaje = mensajeSunatRepositorio.findById(nuCodigoMensaje)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        mensaje.setNuDestacado(destacado);
        return mensajeSunatRepositorio.save(mensaje);
    }

    /**
     * Actualiza el estado urgente de un mensaje
     * @param nuCodigoMensaje ID del mensaje
     * @param urgente 1: urgente, 0: no urgente
     * @return Mensaje actualizado
     */
    public MensajeSunat actualizarUrgente(Long nuCodigoMensaje, Integer urgente) {
        MensajeSunat mensaje = mensajeSunatRepositorio.findById(nuCodigoMensaje)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        mensaje.setNuUrgente(urgente);
        return mensajeSunatRepositorio.save(mensaje);
    }

    /**
     * Actualiza el estado de un mensaje
     * @param nuCodigoMensaje ID del mensaje
     * @param estado 1: activo, 0: inactivo
     * @return Mensaje actualizado
     */
    public MensajeSunat actualizarEstado(Long nuCodigoMensaje, Integer estado) {
        MensajeSunat mensaje = mensajeSunatRepositorio.findById(nuCodigoMensaje)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        mensaje.setNuEstado(estado);
        return mensajeSunatRepositorio.save(mensaje);
    }

    /**
     * Actualiza la etiqueta de un mensaje
     * @param nuCodigoMensaje ID del mensaje
     * @param codigoEtiqueta Código de la etiqueta
     * @return Mensaje actualizado
     */
    public MensajeSunat actualizarEtiqueta(Long nuCodigoMensaje, String codigoEtiqueta) {
        MensajeSunat mensaje = mensajeSunatRepositorio.findById(nuCodigoMensaje)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        mensaje.setVcCodigoEtiqueta(codigoEtiqueta);
        return mensajeSunatRepositorio.save(mensaje);
    }

    /**
     * Actualiza el estado de leído de un mensaje
     * @param nuCodigoMensaje ID del mensaje
     * @param leido 1: leído, 0: no leído
     * @return Mensaje actualizado
     */
    public MensajeSunat actualizarLeido(Long nuCodigoMensaje, Integer leido) {
        MensajeSunat mensaje = mensajeSunatRepositorio.findById(nuCodigoMensaje)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        mensaje.setNuLeido(leido);
        return mensajeSunatRepositorio.save(mensaje);
    }

    /**
     * Actualiza el estado de archivado de un mensaje
     * @param nuCodigoMensaje ID del mensaje
     * @param archivado 1: archivado, 0: no archivado
     * @return Mensaje actualizado
     */
    public MensajeSunat actualizarArchivado(Long nuCodigoMensaje, Integer archivado) {
        MensajeSunat mensaje = mensajeSunatRepositorio.findById(nuCodigoMensaje)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        mensaje.setNuArchivado(archivado);
        return mensajeSunatRepositorio.save(mensaje);
    }
}