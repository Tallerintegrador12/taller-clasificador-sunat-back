package upao.edu.pe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import upao.edu.pe.model.MensajeSunat;

import java.util.List;

@Service
@Slf4j
public class EmailMonitoringService {

    @Autowired
    private MensajeSunatServicio mensajeSunatServicio;

    @Value("${app.monitoring.enabled:true}")
    private boolean monitoringEnabled;

    @Value("${app.monitoring.default-ruc:20000000001}")
    private String defaultRuc;

    @Value("${app.monitoring.max-emails-per-check:5}")
    private int maxEmailsPerCheck;

    /**
     * Verifica periódicamente si hay correos nuevos para procesar
     * Se ejecuta cada 5 minutos por defecto
     */
    @Scheduled(fixedRate = 300000) // 5 minutos en milisegundos
    public void verificarCorreosNuevos() {
        if (!monitoringEnabled) {
            return;
        }

        try {
            log.debug("Verificando correos nuevos...");
            
            // Obtener correos no etiquetados (nuevos)
            List<MensajeSunat> correosNuevos = mensajeSunatServicio.obtenerTodosMensajes(defaultRuc)
                .stream()
                .filter(correo -> "00".equals(correo.getVcCodigoEtiqueta()))
                .limit(maxEmailsPerCheck)
                .toList();

            if (!correosNuevos.isEmpty()) {
                log.info("🔍 Se encontraron {} correos nuevos para procesar", correosNuevos.size());
                
                // Procesar correos automáticamente
                mensajeSunatServicio.procesarNuevosCorreosConIA(correosNuevos);
                
                log.info("✅ Correos nuevos procesados automáticamente");
            } else {
                log.debug("No se encontraron correos nuevos");
            }

        } catch (Exception e) {
            log.error("Error en verificación automática de correos: {}", e.getMessage());
        }
    }

    /**
     * Verifica correos nuevos manualmente (puede ser llamado desde API)
     */
    public void verificarCorreosNuevosManuales(String ruc, int limit) {
        log.info("Verificación manual de correos nuevos iniciada para RUC: {}", ruc);
        
        try {
            List<MensajeSunat> correosNuevos = mensajeSunatServicio.obtenerTodosMensajes(ruc)
                .stream()
                .filter(correo -> "00".equals(correo.getVcCodigoEtiqueta()))
                .limit(limit)
                .toList();

            if (!correosNuevos.isEmpty()) {
                log.info("📧 Procesando {} correos nuevos manualmente", correosNuevos.size());
                mensajeSunatServicio.procesarNuevosCorreosConIA(correosNuevos);
            } else {
                log.info("No hay correos nuevos para procesar");
            }

        } catch (Exception e) {
            log.error("Error en verificación manual: {}", e.getMessage());
            throw new RuntimeException("Error al verificar correos: " + e.getMessage());
        }
    }

    /**
     * Simula la llegada de correos nuevos para testing
     */
    public void simularCorreosNuevos() {
        log.info("🧪 Simulando llegada de correos nuevos...");
        
        try {
            // Obtener algunos correos existentes para simular
            List<MensajeSunat> correosParaSimular = mensajeSunatServicio.obtenerTodosMensajes(defaultRuc)
                .stream()
                .limit(3)
                .toList();

            if (!correosParaSimular.isEmpty()) {
                // Resetear su etiqueta para simular que son nuevos
                correosParaSimular.forEach(correo -> correo.setVcCodigoEtiqueta("00"));
                
                // Procesar como nuevos
                mensajeSunatServicio.procesarNuevosCorreosConIA(correosParaSimular);
                
                log.info("✅ Simulación completada con {} correos", correosParaSimular.size());
            } else {
                log.warn("No hay correos disponibles para simular");
            }

        } catch (Exception e) {
            log.error("Error en simulación: {}", e.getMessage());
            throw new RuntimeException("Error al simular correos: " + e.getMessage());
        }
    }
}
