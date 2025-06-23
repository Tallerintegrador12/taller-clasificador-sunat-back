package upao.edu.pe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import upao.edu.pe.model.MensajeSunat;

import java.util.List;

@Service
@Slf4j
public class NotificationService {

    @Value("${app.notifications.enabled:true}")
    private boolean notificationsEnabled;

    @Value("${app.notifications.sound.enabled:true}")
    private boolean soundEnabled;

    /**
     * Notifica sobre correos nuevos analizados
     */
    public void notifyNewEmails(List<EmailAnalysisInfo> analyzedEmails) {
        if (!notificationsEnabled) {
            return;
        }

        int totalEmails = analyzedEmails.size();
        
        if (totalEmails == 0) {
            return;
        }

        log.info("=== NOTIFICACIÓN DE CORREOS NUEVOS ===");
        log.info("📧 Total de correos nuevos: {}", totalEmails);
        
        // Agrupar por clasificación
        long muyImportantes = analyzedEmails.stream()
                .filter(email -> "MUY IMPORTANTE".equals(email.getClasificacion()))
                .count();
        
        long importantes = analyzedEmails.stream()
                .filter(email -> "IMPORTANTE".equals(email.getClasificacion()))
                .count();
                
        long recurrentes = analyzedEmails.stream()
                .filter(email -> "RECURRENTE".equals(email.getClasificacion()))
                .count();

        // Mostrar resumen por clasificación
        if (muyImportantes > 0) {
            log.info("🔴 Muy Importantes: {}", muyImportantes);
        }
        if (importantes > 0) {
            log.info("🟡 Importantes: {}", importantes);
        }
        if (recurrentes > 0) {
            log.info("🟢 Recurrentes: {}", recurrentes);
        }

        log.info("--- Detalle de correos ---");
        
        // Mostrar detalle de cada correo
        for (EmailAnalysisInfo email : analyzedEmails) {
            String emoji = getClasificacionEmoji(email.getClasificacion());
            log.info("{} {} | {} | {}", 
                emoji, 
                email.getClasificacion(),
                email.getEtiquetaNombre(), 
                truncateSubject(email.getAsunto(), 50)
            );
        }

        log.info("=====================================");

        // Emitir sonido si está habilitado
        if (soundEnabled && muyImportantes > 0) {
            emitNotificationSound();
        }
    }

    /**
     * Notifica un solo correo nuevo
     */
    public void notifySingleEmail(EmailAnalysisInfo emailInfo) {
        if (!notificationsEnabled) {
            return;
        }

        String emoji = getClasificacionEmoji(emailInfo.getClasificacion());
        
        log.info("📬 NUEVO CORREO RECIBIDO:");
        log.info("   {} Clasificación: {}", emoji, emailInfo.getClasificacion());
        log.info("   🏷️  Etiqueta: {}", emailInfo.getEtiquetaNombre());
        log.info("   📝 Asunto: {}", truncateSubject(emailInfo.getAsunto(), 80));
        log.info("   👤 De: {}", emailInfo.getEmisor());
        
        if (emailInfo.getRazon() != null && !emailInfo.getRazon().isEmpty()) {
            log.info("   🤖 IA: {}", emailInfo.getRazon());
        }

        // Emitir sonido para correos muy importantes
        if (soundEnabled && "MUY IMPORTANTE".equals(emailInfo.getClasificacion())) {
            emitNotificationSound();
        }
    }

    /**
     * Obtiene el emoji correspondiente a la clasificación
     */
    private String getClasificacionEmoji(String clasificacion) {
        switch (clasificacion) {
            case "MUY IMPORTANTE":
                return "🔴";
            case "IMPORTANTE":
                return "🟡";
            case "RECURRENTE":
                return "🟢";
            default:
                return "⚪";
        }
    }

    /**
     * Trunca el asunto del correo para la notificación
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
     * Emite un sonido de notificación (simulado con logs)
     */
    private void emitNotificationSound() {
        log.info("🔊 ¡SONIDO DE NOTIFICACIÓN! - Correo muy importante recibido");
        
        // En un entorno real, aquí podrías:
        // 1. Tocar un archivo de sonido
        // 2. Enviar una notificación push
        // 3. Enviar un email/SMS al administrador
        // 4. Integrar con Slack/Teams/Discord
    }

    /**
     * Clase para información de análisis de email
     */
    public static class EmailAnalysisInfo {
        private String asunto;
        private String emisor;
        private String clasificacion;
        private String etiquetaNombre;
        private String etiquetaCodigo;
        private String razon;

        public EmailAnalysisInfo() {}

        public EmailAnalysisInfo(String asunto, String emisor, String clasificacion, 
                               String etiquetaNombre, String etiquetaCodigo, String razon) {
            this.asunto = asunto;
            this.emisor = emisor;
            this.clasificacion = clasificacion;
            this.etiquetaNombre = etiquetaNombre;
            this.etiquetaCodigo = etiquetaCodigo;
            this.razon = razon;
        }

        // Getters y Setters
        public String getAsunto() { return asunto; }
        public void setAsunto(String asunto) { this.asunto = asunto; }
        
        public String getEmisor() { return emisor; }
        public void setEmisor(String emisor) { this.emisor = emisor; }
        
        public String getClasificacion() { return clasificacion; }
        public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }
        
        public String getEtiquetaNombre() { return etiquetaNombre; }
        public void setEtiquetaNombre(String etiquetaNombre) { this.etiquetaNombre = etiquetaNombre; }
        
        public String getEtiquetaCodigo() { return etiquetaCodigo; }
        public void setEtiquetaCodigo(String etiquetaCodigo) { this.etiquetaCodigo = etiquetaCodigo; }
        
        public String getRazon() { return razon; }
        public void setRazon(String razon) { this.razon = razon; }

        public static EmailAnalysisInfo fromMensajeAndAnalysis(MensajeSunat mensaje, 
                                                             GeminiAIService.EmailAnalysisResult analysis) {
            return new EmailAnalysisInfo(
                mensaje.getVcAsunto(),
                mensaje.getVcUsuarioEmisor(),
                analysis.getClasificacion(),
                analysis.getEtiquetaNombre(),
                analysis.getEtiquetaCodigo(),
                analysis.getRazon()
            );
        }
    }
}
