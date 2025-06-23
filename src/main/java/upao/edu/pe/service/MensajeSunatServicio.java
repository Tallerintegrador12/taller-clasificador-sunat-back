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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MensajeSunatServicio {

    private static final Logger log = (Logger) LoggerFactory.getLogger(MensajeSunatServicio.class);

    @Autowired
    private MensajeSunatRepositorio mensajeSunatRepositorio;

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private NotificationService notificationService;

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
    public List<MensajeSunat> obtenerTodosMensajes(String vc_numero_ruc) {
        // Si el RUC está vacío, devolver todos los mensajes (para debugging)
        if (vc_numero_ruc == null || vc_numero_ruc.trim().isEmpty()) {
            return mensajeSunatRepositorio.findAll();
        }
        return mensajeSunatRepositorio.findMensajesOrdenadosPorFecha(vc_numero_ruc);
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

    /**
     * Obtiene un mensaje por su ID
     * @param id ID del mensaje
     * @return Mensaje encontrado o null si no existe
     */
    public MensajeSunat obtenerMensajePorId(Long id) {
        return mensajeSunatRepositorio.findById(id).orElse(null);
    }

    /**
     * Procesa nuevos correos con análisis de Gemini AI
     * @param nuevosCorreos Lista de nuevos correos a procesar
     * @return Lista de correos procesados con clasificación automática
     */
    public List<MensajeSunat> procesarNuevosCorreosConIA(List<MensajeSunat> nuevosCorreos) {
        if (nuevosCorreos == null || nuevosCorreos.isEmpty()) {
            return nuevosCorreos;
        }

        log.info("Procesando {} nuevos correos con Gemini AI", nuevosCorreos.size());
        
        List<NotificationService.EmailAnalysisInfo> correosProcesados = new ArrayList<>();
        List<MensajeSunat> correosActualizados = new ArrayList<>();

        for (MensajeSunat correo : nuevosCorreos) {
            try {
                // Analizar correo con Gemini AI
                GeminiAIService.EmailAnalysisResult analysis = geminiAIService.analyzeEmail(correo);
                
                // Actualizar correo con la clasificación
                correo.setVcCodigoEtiqueta(analysis.getEtiquetaCodigo());
                
                // Determinar clasificación (Muy Importante, Importante, Recurrente)
                asignarClasificacion(correo, analysis.getClasificacion());
                
                // Guardar correo actualizado
                MensajeSunat correoGuardado = mensajeSunatRepositorio.save(correo);
                correosActualizados.add(correoGuardado);
                
                // Crear info para notificación
                NotificationService.EmailAnalysisInfo emailInfo = 
                    NotificationService.EmailAnalysisInfo.fromMensajeAndAnalysis(correo, analysis);
                correosProcesados.add(emailInfo);
                
                log.info("Correo procesado: {} -> {} ({})", 
                    truncateSubject(correo.getVcAsunto(), 30), 
                    analysis.getClasificacion(),
                    analysis.getEtiquetaNombre());
                
            } catch (Exception e) {
                log.error("Error procesando correo {}: {}", correo.getNuCodigoMensaje(), e.getMessage());
                
                // Asignar clasificación por defecto en caso de error
                correo.setVcCodigoEtiqueta("00");
                MensajeSunat correoGuardado = mensajeSunatRepositorio.save(correo);
                correosActualizados.add(correoGuardado);
            }
        }

        // Enviar notificaciones
        if (!correosProcesados.isEmpty()) {
            notificationService.notifyNewEmails(correosProcesados);
        }

        return correosActualizados;
    }

    /**
     * Procesa un solo correo nuevo con análisis de IA
     * @param correo Correo a procesar
     * @return Correo procesado
     */
    public MensajeSunat procesarCorreoIndividualConIA(MensajeSunat correo) {
        try {
            // Analizar correo con Gemini AI
            GeminiAIService.EmailAnalysisResult analysis = geminiAIService.analyzeEmail(correo);
            
            // Actualizar correo con la clasificación
            correo.setVcCodigoEtiqueta(analysis.getEtiquetaCodigo());
            
            // Determinar clasificación
            asignarClasificacion(correo, analysis.getClasificacion());
            
            // Guardar correo actualizado
            MensajeSunat correoGuardado = mensajeSunatRepositorio.save(correo);
            
            // Crear info para notificación
            NotificationService.EmailAnalysisInfo emailInfo = 
                NotificationService.EmailAnalysisInfo.fromMensajeAndAnalysis(correo, analysis);
            
            // Notificar correo individual
            notificationService.notifySingleEmail(emailInfo);
            
            log.info("Correo individual procesado: {} -> {} ({})", 
                truncateSubject(correo.getVcAsunto(), 30), 
                analysis.getClasificacion(),
                analysis.getEtiquetaNombre());
            
            return correoGuardado;
            
        } catch (Exception e) {
            log.error("Error procesando correo individual {}: {}", correo.getNuCodigoMensaje(), e.getMessage());
            
            // Asignar clasificación por defecto
            correo.setVcCodigoEtiqueta("00");
            return mensajeSunatRepositorio.save(correo);
        }
    }

    /**
     * Asigna la clasificación al correo basada en el análisis de IA
     * SOLO actualiza el campo clasificacion - NO toca nu_urgente ni nu_destacado
     * porque esos son controles manuales del usuario:
     * - nu_urgente: Banderita manual en SUNAT 🚩  
     * - nu_destacado: Estrella manual en frontend ⭐
     */
    private void asignarClasificacion(MensajeSunat correo, String clasificacion) {
        // Solo asignar la clasificación como texto
        correo.setClasificacion(clasificacion);
        
        // NO modificar nu_urgente ni nu_destacado - son controles del usuario
    }

    /**
     * Trunca el asunto del correo para logs
     */
    private String truncateSubject(String subject, int maxLength) {
        if (subject == null) {
            return "Sin asunto";
        }
        if (subject.length() <= maxLength) {
            return subject;
        }
        return subject.substring(0, maxLength - 3) + "...";
    }

    /**
     * Obtiene estadísticas de correos procesados
     */
    public Map<String, Object> obtenerEstadisticasCorreos() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total de correos
        long totalCorreos = mensajeSunatRepositorio.count();
        stats.put("totalCorreos", totalCorreos);
        
        // Correos por etiqueta
        Map<String, Long> correosPorEtiqueta = new HashMap<>();
        for (String codigo : ETIQUETAS.keySet()) {
            long count = mensajeSunatRepositorio.countByVcCodigoEtiqueta(codigo);
            correosPorEtiqueta.put(ETIQUETAS.get(codigo), count);
        }
        stats.put("correosPorEtiqueta", correosPorEtiqueta);
        
        // Correos destacados
        long correosDestacados = mensajeSunatRepositorio.countByNuDestacado(1);
        stats.put("correosDestacados", correosDestacados);
        
        // Correos urgentes
        long correosUrgentes = mensajeSunatRepositorio.countByNuUrgente(1);
        stats.put("correosUrgentes", correosUrgentes);
        
        return stats;
    }
}