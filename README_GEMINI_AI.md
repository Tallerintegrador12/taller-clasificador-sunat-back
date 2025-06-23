# 🤖 Integración Gemini AI - Sistema de Notificaciones SUNAT

Este sistema integra Gemini AI para clasificar automáticamente los correos de SUNAT y enviar notificaciones cuando lleguen correos nuevos.

## 🚀 Funcionalidades

### 1. Clasificación Automática con IA
- **Muy Importante**: Correos urgentes que requieren atención inmediata
- **Importante**: Correos que requieren atención pero no son urgentes  
- **Recurrente**: Correos informativos o de rutina

### 2. Asignación Automática de Etiquetas
- **NO ETIQUETADOS (00)**: Sin clasificar
- **VALORES (10)**: Pagos, valores, montos
- **RESOLUCIONES DE COBRANZAS (11)**: Cobranzas, embargos, retenciones
- **RESOLUCIONES NO CONTENCIOSAS (13)**: Resoluciones administrativas
- **RESOLUCIONES DE FISCALIZACION (14)**: Auditorías, fiscalizaciones
- **RESOLUCIONES ANTERIORES (15)**: Resoluciones históricas
- **AVISOS (16)**: Notificaciones generales

### 3. Sistema de Notificaciones
- Notificaciones en logs con emojis y colores
- Resumen por clasificación
- Detalle de cada correo procesado
- Sonido simulado para correos muy importantes

## ⚙️ Configuración

### 1. API Key de Gemini
Edita el archivo `src/main/resources/application.properties`:

```properties
# Configuración de Gemini AI
gemini.api.key=TU_API_KEY_DE_GEMINI_AQUI
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
gemini.enabled=true

# Configuración de notificaciones
app.notifications.enabled=true
app.notifications.sound.enabled=true

# Configuración de monitoreo automático
app.monitoring.enabled=true
app.monitoring.default-ruc=20000000001
app.monitoring.max-emails-per-check=5
```

### 2. Obtener API Key de Gemini
1. Ve a [Google AI Studio](https://aistudio.google.com/)
2. Crea una cuenta o inicia sesión
3. Genera una nueva API Key
4. Copia la clave y pégala en `gemini.api.key`

## 🔧 Endpoints Disponibles

### 1. Verificar Correos Nuevos (Manual)
```
POST /api/sunat/verificar-correos-nuevos?ruc=20000000001&limit=5
```
- Verifica manualmente si hay correos nuevos
- Los clasifica con Gemini AI
- Envía notificaciones

### 2. Simular Correos Nuevos (Testing)
```
POST /api/sunat/simular-correos-nuevos
```
- Simula la llegada de correos nuevos
- Perfecto para testing
- Muestra todas las notificaciones

### 3. Procesar Correos Específicos
```
POST /api/sunat/procesar-correos-nuevos?ruc=20000000001&limit=10
```
- Procesa correos no etiquetados
- Retorna estadísticas detalladas

### 4. Endpoints de Testing (Gemini)
```
POST /api/gemini/analizar-correo/{id}      # Analizar un correo específico
POST /api/gemini/procesar-correos-lote     # Procesar múltiples correos
GET  /api/gemini/estadisticas              # Obtener estadísticas
POST /api/gemini/simular-nuevos-correos    # Simular para testing
```

## 🤖 Monitoreo Automático

El sistema verifica automáticamente cada **5 minutos** si hay correos nuevos:

- ✅ **Habilitado por defecto**
- 🔍 Verifica correos con etiqueta "00" (no etiquetados)
- 📊 Procesa máximo 5 correos por verificación
- 📝 Registra todo en logs

Para deshabilitarlo:
```properties
app.monitoring.enabled=false
```

## 📋 Ejemplo de Notificaciones

Cuando llegan correos nuevos, verás en los logs:

```
=== NOTIFICACIÓN DE CORREOS NUEVOS ===
📧 Total de correos nuevos: 3
🔴 Muy Importantes: 1
🟡 Importantes: 1  
🟢 Recurrentes: 1
--- Detalle de correos ---
🔴 MUY IMPORTANTE | RESOLUCIONES DE COBRANZAS | Resolución de embargo preventivo...
🟡 IMPORTANTE | VALORES | Liquidación de pagos pendientes...
🟢 RECURRENTE | AVISOS | Actualización de normativa tributaria...
=====================================
🔊 ¡SONIDO DE NOTIFICACIÓN! - Correo muy importante recibido
```

## 🧪 Testing

### 1. Usar Simulación
```bash
curl -X POST "http://localhost:8080/api/sunat/simular-correos-nuevos"
```

### 2. Verificar Logs
Observa la consola para ver las notificaciones con:
- 📧 Total de correos
- 🔴🟡🟢 Clasificaciones por color
- 🏷️ Etiquetas asignadas
- 🤖 Explicación de la IA

### 3. Testing con Postman
Importa los endpoints y prueba con diferentes RUCs y límites.

## 🔧 Personalización

### Cambiar Intervalos de Monitoreo
En `EmailMonitoringService.java`:
```java
@Scheduled(fixedRate = 300000) // 5 minutos
```

### Personalizar Clasificaciones
En `GeminiAIService.java` puedes modificar el prompt para ajustar las clasificaciones.

### Modificar Notificaciones
En `NotificationService.java` puedes:
- Cambiar emojis
- Integrar con servicios externos (Slack, Teams, etc.)
- Añadir sonidos reales
- Enviar emails/SMS

## ⚠️ Consideraciones Importantes

1. **API Key Segura**: No hardcodees la API key en el código
2. **Límites de Rate**: Gemini tiene límites de requests por minuto
3. **Logs**: Las notificaciones aparecen en los logs de la aplicación
4. **Testing**: Usa la simulación para probar sin consumir API calls reales

## 🎯 Próximos Pasos

1. Configura tu API Key de Gemini
2. Ejecuta el proyecto: `mvn spring-boot:run`
3. Prueba la simulación: `POST /api/sunat/simular-correos-nuevos`
4. Observa las notificaciones en la consola
5. Integra con sistemas de notificación reales si lo necesitas

¡El sistema ya está listo para notificar automáticamente sobre correos nuevos de SUNAT con clasificación inteligente! 🚀
