package upao.edu.pe.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para las respuestas del Asistente Virtual Contable
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenteRespuestaDTO {
    
    /**
     * Respuesta generada por el asistente
     */
    private String respuesta;
    
    /**
     * Confianza de la respuesta (0.0 a 1.0)
     */
    private Double confianza;
    
    /**
     * Categoría de la consulta detectada
     */
    private String categoria;
    
    /**
     * Fuentes o referencias utilizadas
     */
    private List<String> fuentes;
    
    /**
     * Recomendaciones adicionales
     */
    private List<String> recomendaciones;
    
    /**
     * Tiempo de respuesta en milisegundos
     */
    private Long tiempoRespuesta;
    
    /**
     * Timestamp de la respuesta
     */
    private LocalDateTime timestamp;
    
    /**
     * Indica si la respuesta requiere seguimiento
     */
    private boolean requiereSeguimiento;
    
    /**
     * ID de la sesión de chat
     */
    private String sesionId;
}
