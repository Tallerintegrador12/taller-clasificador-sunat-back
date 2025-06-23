package upao.edu.pe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeminiModelSelector {
    
    @Value("${gemini.api.url}")
    private String defaultModelUrl;
    
    // URLs para diferentes modelos
    private static final String FLASH_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final String PRO_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";
    
    /**
     * Selecciona el modelo óptimo basado en el tipo de consulta
     */
    public String selectOptimalModel(String consulta, String contexto) {
        
        // Usar Gemini-1.5-Pro para consultas complejas
        if (esConsultaCompleja(consulta)) {
            log.info("🧠 Usando Gemini-1.5-Pro para consulta compleja");
            return PRO_URL;
        }
        
        // Usar Gemini-1.5-Flash para consultas rápidas
        log.info("⚡ Usando Gemini-1.5-Flash para consulta rápida");
        return FLASH_URL;
    }
    
    /**
     * Determina si una consulta requiere el modelo Pro
     */
    private boolean esConsultaCompleja(String consulta) {
        String consultaLower = consulta.toLowerCase();
        
        // Palabras clave que indican complejidad
        String[] palabrasComplejas = {
            "analiza", "calcula", "proyecta", "compara", 
            "explícame detalladamente", "paso a paso",
            "estrategia", "planificación", "optimización",
            "múltiples escenarios", "análisis financiero",
            "predicción", "tendencia", "impacto",
            "legislación", "normativa compleja"
        };
        
        for (String palabra : palabrasComplejas) {
            if (consultaLower.contains(palabra)) {
                return true;
            }
        }
        
        // Si la consulta es muy larga (más de 200 caracteres)
        if (consulta.length() > 200) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Obtiene configuraciones específicas para cada modelo
     */
    public ModelConfig getModelConfig(String modelUrl) {
        if (PRO_URL.equals(modelUrl)) {
            return ModelConfig.builder()
                .maxTokens(4096)
                .temperature(0.2)
                .maxRequestsPerMinute(100)
                .timeoutSeconds(30)
                .build();
        } else {
            return ModelConfig.builder()
                .maxTokens(2048)
                .temperature(0.3)
                .maxRequestsPerMinute(200)
                .timeoutSeconds(10)
                .build();
        }
    }
    
    public static class ModelConfig {
        private int maxTokens;
        private double temperature;
        private int maxRequestsPerMinute;
        private int timeoutSeconds;
        
        public static ModelConfigBuilder builder() {
            return new ModelConfigBuilder();
        }
        
        // Constructor, getters, setters
        public ModelConfig() {}
        
        public ModelConfig(int maxTokens, double temperature, int maxRequestsPerMinute, int timeoutSeconds) {
            this.maxTokens = maxTokens;
            this.temperature = temperature;
            this.maxRequestsPerMinute = maxRequestsPerMinute;
            this.timeoutSeconds = timeoutSeconds;
        }
        
        // Getters y Setters
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
        
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        
        public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
        public void setMaxRequestsPerMinute(int maxRequestsPerMinute) { this.maxRequestsPerMinute = maxRequestsPerMinute; }
        
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
        
        public static class ModelConfigBuilder {
            private int maxTokens;
            private double temperature;
            private int maxRequestsPerMinute;
            private int timeoutSeconds;
            
            public ModelConfigBuilder maxTokens(int maxTokens) {
                this.maxTokens = maxTokens;
                return this;
            }
            
            public ModelConfigBuilder temperature(double temperature) {
                this.temperature = temperature;
                return this;
            }
            
            public ModelConfigBuilder maxRequestsPerMinute(int maxRequestsPerMinute) {
                this.maxRequestsPerMinute = maxRequestsPerMinute;
                return this;
            }
            
            public ModelConfigBuilder timeoutSeconds(int timeoutSeconds) {
                this.timeoutSeconds = timeoutSeconds;
                return this;
            }
            
            public ModelConfig build() {
                return new ModelConfig(maxTokens, temperature, maxRequestsPerMinute, timeoutSeconds);
            }
        }
    }
}
