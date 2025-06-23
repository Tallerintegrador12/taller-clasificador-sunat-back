package upao.edu.pe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upao.edu.pe.dto.response.RespuestaControlador;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.model.Etiqueta;
import upao.edu.pe.service.EtiquetaServicio;
import upao.edu.pe.service.MensajeSunatServicio;
import upao.edu.pe.service.SunatNotificacionService;
import upao.edu.pe.service.SunatServicio;
import upao.edu.pe.service.EmailMonitoringService;
import upao.edu.pe.service.GeminiAIService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sunat")
@CrossOrigin(originPatterns = "*", allowCredentials = "false")
@Tag(name = "Sunat Controlador", description = "API para la gestión de mensajes SUNAT")
public class SunatControlador {

    @Autowired
    private SunatServicio sunatServicio;

    @Autowired
    private MensajeSunatServicio mensajeSunatServicio;

    @Autowired
    private SunatNotificacionService sunatNotificacionService;

    @Autowired
    private EtiquetaServicio etiquetaServicio;

    @Autowired
    private EmailMonitoringService emailMonitoringService;

    @Autowired
    private GeminiAIService geminiAIService;

    /**
     * Inicia el proceso de sincronización manual
     */
    @GetMapping("/sincronizar")
    public ResponseEntity<RespuestaControlador<String>> sincronizarMensajes(
            @RequestParam("cookie") String cookieSunat) {

        sunatServicio.SP_CONSULTAR_Y_GUARDAR_MENSAJES(cookieSunat);

        return new ResponseEntity<>(
                RespuestaControlador.exito("Sincronización iniciada manualmente", null),
                HttpStatus.OK
        );
    }

    /**
     * Obtiene todos los mensajes sin paginación (método original) - Solo etiqueta "00"
     */
   /* @GetMapping("/obtener-detalle-mensaje")
    public ResponseEntity<RespuestaControlador<List<MensajeSunat>>> obtenerDetalleMensajes() {
        List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes();
        for(MensajeSunat mensaje : mensajes){
            sunatNotificacionService.procesarYGuardarNotificacion(mensaje.getNuCodigoMensaje().toString());
        }

        return new ResponseEntity<>(
                RespuestaControlador.exito(mensajes),
                HttpStatus.OK
        );
    }*/

    /**
     * Obtiene todos los mensajes sin paginación (método original) - Solo etiqueta "00"
     */
    @Operation(summary = "Obtener mensajes SUNAT", 
               description = "Obtiene todos los mensajes SUNAT para un RUC específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mensajes obtenidos exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/mensajes")
    public ResponseEntity<RespuestaControlador<List<MensajeSunat>>> obtenerMensajes(
            @Parameter(description = "Número de RUC del contribuyente", required = true, example = "20123456789")
            @RequestParam("vc_numero_ruc") String vc_numero_ruc) {
        List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes(vc_numero_ruc);

        return new ResponseEntity<>(
                RespuestaControlador.exito(mensajes),
                HttpStatus.OK
        );
    }

    /**
     * Obtiene mensajes paginados - Solo etiqueta "00"
     * @param pagina Número de página (comienza en 0)
     * @param cantidad Cantidad de elementos por página (por defecto 10)
     */
    @GetMapping("/mensajes/paginados")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerMensajesPaginados(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int cantidad) {

        Page<MensajeSunat> paginaMensajes = mensajeSunatServicio.obtenerMensajesPaginados(pagina, cantidad);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensajes", paginaMensajes.getContent());
        respuesta.put("paginaActual", paginaMensajes.getNumber());
        respuesta.put("totalElementos", paginaMensajes.getTotalElements());
        respuesta.put("totalPaginas", paginaMensajes.getTotalPages());

        return new ResponseEntity<>(
                RespuestaControlador.exito(respuesta),
                HttpStatus.OK
        );
    }

    /**
     * Obtiene mensajes filtrados por etiqueta
     * @param etiqueta Código de etiqueta (10, 11, 13, 14, 15, 16)
     */
    @GetMapping("/mensajes/etiqueta/{etiqueta}")
    public ResponseEntity<RespuestaControlador<List<MensajeSunat>>> obtenerMensajesPorEtiqueta(
            @PathVariable String etiqueta) {

        List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerMensajesPorEtiqueta(etiqueta);
        String descripcionEtiqueta = mensajeSunatServicio.obtenerDescripcionEtiqueta(etiqueta);

        return new ResponseEntity<>(
                RespuestaControlador.exito("Mensajes de etiqueta: " + descripcionEtiqueta, mensajes),
                HttpStatus.OK
        );
    }

    /**
     * Obtiene mensajes paginados filtrados por etiqueta
     * @param etiqueta Código de etiqueta (10, 11, 13, 14, 15, 16)
     * @param pagina Número de página (comienza en 0)
     * @param cantidad Cantidad de elementos por página (por defecto 10)
     */
    @GetMapping("/mensajes/etiqueta/{etiqueta}/paginados")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerMensajesPorEtiquetaPaginados(
            @PathVariable String etiqueta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int cantidad) {

        Page<MensajeSunat> paginaMensajes = mensajeSunatServicio.obtenerMensajesPorEtiquetaPaginados(etiqueta, pagina, cantidad);
        String descripcionEtiqueta = mensajeSunatServicio.obtenerDescripcionEtiqueta(etiqueta);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensajes", paginaMensajes.getContent());
        respuesta.put("paginaActual", paginaMensajes.getNumber());
        respuesta.put("totalElementos", paginaMensajes.getTotalElements());
        respuesta.put("totalPaginas", paginaMensajes.getTotalPages());
        respuesta.put("etiqueta", etiqueta);
        respuesta.put("descripcionEtiqueta", descripcionEtiqueta);

        return new ResponseEntity<>(
                RespuestaControlador.exito("Mensajes de etiqueta: " + descripcionEtiqueta, respuesta),
                HttpStatus.OK
        );
    }

    /**
     * Obtiene el listado de etiquetas disponibles
     */
    @GetMapping("/etiquetas")
    public ResponseEntity<RespuestaControlador<Map<String, String>>> obtenerEtiquetas() {
        Map<String, String> etiquetas = mensajeSunatServicio.obtenerTodasLasEtiquetas();
        return new ResponseEntity<>(
                RespuestaControlador.exito("Listado de etiquetas disponibles", etiquetas),
                HttpStatus.OK
        );
    }

    // ========== NUEVOS ENDPOINTS ==========

    /**
     * Actualiza el estado destacado de un mensaje
     */
    @PutMapping("/mensajes/{id}/destacado")
    public ResponseEntity<RespuestaControlador<MensajeSunat>> actualizarDestacado(
            @PathVariable Long id,
            @RequestParam Integer destacado) {

        MensajeSunat mensaje = mensajeSunatServicio.actualizarDestacado(id, destacado);
        String estadoTexto = destacado == 1 ? "destacado" : "no destacado";

        return new ResponseEntity<>(
                RespuestaControlador.exito("Mensaje marcado como " + estadoTexto, mensaje),
                HttpStatus.OK
        );
    }

    /**
     * Actualiza el estado urgente de un mensaje
     */
    @PutMapping("/mensajes/{id}/urgente")
    public ResponseEntity<RespuestaControlador<MensajeSunat>> actualizarUrgente(
            @PathVariable Long id,
            @RequestParam Integer urgente) {

        MensajeSunat mensaje = mensajeSunatServicio.actualizarUrgente(id, urgente);
        String estadoTexto = urgente == 1 ? "urgente" : "no urgente";

        return new ResponseEntity<>(
                RespuestaControlador.exito("Mensaje marcado como " + estadoTexto, mensaje),
                HttpStatus.OK
        );
    }

    /**
     * Actualiza el estado de un mensaje
     */
    @PutMapping("/mensajes/{id}/estado")
    public ResponseEntity<RespuestaControlador<MensajeSunat>> actualizarEstado(
            @PathVariable Long id,
            @RequestParam Integer estado) {

        MensajeSunat mensaje = mensajeSunatServicio.actualizarEstado(id, estado);
        String estadoTexto = estado == 1 ? "activo" : "inactivo";

        return new ResponseEntity<>(
                RespuestaControlador.exito("Mensaje marcado como " + estadoTexto, mensaje),
                HttpStatus.OK
        );
    }

    /**
     * Actualiza la etiqueta de un mensaje
     */
    @PutMapping("/mensajes/{id}/etiqueta")
    public ResponseEntity<RespuestaControlador<MensajeSunat>> actualizarEtiqueta(
            @PathVariable Long id,
            @RequestParam String codigoEtiqueta) {

        MensajeSunat mensaje = mensajeSunatServicio.actualizarEtiqueta(id, codigoEtiqueta);
        String descripcionEtiqueta = mensajeSunatServicio.obtenerDescripcionEtiqueta(codigoEtiqueta);

        return new ResponseEntity<>(
                RespuestaControlador.exito("Etiqueta actualizada a: " + descripcionEtiqueta, mensaje),
                HttpStatus.OK
        );
    }

    /**
     * Actualiza el estado de leído de un mensaje
     */
    @PutMapping("/mensajes/{id}/leido")
    public ResponseEntity<RespuestaControlador<MensajeSunat>> actualizarLeido(
            @PathVariable Long id,
            @RequestParam Integer leido) {

        MensajeSunat mensaje = mensajeSunatServicio.actualizarLeido(id, leido);
        String estadoTexto = leido == 1 ? "leído" : "no leído";

        return new ResponseEntity<>(
                RespuestaControlador.exito("Mensaje marcado como " + estadoTexto, mensaje),
                HttpStatus.OK
        );
    }

    /**
     * Actualiza el estado de archivado de un mensaje
     */
    @PutMapping("/mensajes/{id}/archivado")
    public ResponseEntity<RespuestaControlador<MensajeSunat>> actualizarArchivado(
            @PathVariable Long id,
            @RequestParam Integer archivado) {

        MensajeSunat mensaje = mensajeSunatServicio.actualizarArchivado(id, archivado);
        String estadoTexto = archivado == 1 ? "archivado" : "no archivado";

        return new ResponseEntity<>(
                RespuestaControlador.exito("Mensaje marcado como " + estadoTexto, mensaje),
                HttpStatus.OK
        );
    }

    /**
     * Actualiza las clasificaciones de todos los mensajes existentes
     */
    @PostMapping("/actualizar-clasificaciones")
    public ResponseEntity<RespuestaControlador<String>> actualizarClasificaciones() {
        try {
            sunatServicio.actualizarClasificacionesExistentes();
            return new ResponseEntity<>(
                    RespuestaControlador.exito("Clasificaciones actualizadas correctamente", null),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            RespuestaControlador<String> respuestaError = new RespuestaControlador<>();
            respuestaError.setVcMensaje("Error al actualizar clasificaciones: " + e.getMessage());
            respuestaError.setNuCodigo(500);
            respuestaError.setDatos(null);
            
            return new ResponseEntity<>(
                    respuestaError,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Procesa correos nuevos con análisis de Gemini AI y notificaciones automáticas
     */
    @Operation(summary = "Procesar correos nuevos con IA", 
               description = "Analiza correos nuevos usando Gemini AI, los clasifica, asigna etiquetas y envía notificaciones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Correos procesados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/procesar-correos-nuevos")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> procesarCorreosNuevos(
            @Parameter(description = "RUC del contribuyente", required = true)
            @RequestParam("ruc") String ruc,
            @Parameter(description = "Límite de correos a procesar", required = false)
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        
        try {
            // Obtener correos recientes que no han sido procesados (etiqueta "00")
            List<MensajeSunat> correosNuevos = mensajeSunatServicio.obtenerTodosMensajes(ruc)
                .stream()
                .filter(correo -> "00".equals(correo.getVcCodigoEtiqueta())) // Solo no etiquetados
                .limit(limit)
                .toList();

            if (correosNuevos.isEmpty()) {
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("mensaje", "No hay correos nuevos para procesar");
                resultado.put("correosNuevos", 0);
                
                return new ResponseEntity<>(
                    RespuestaControlador.exito("No hay correos nuevos", resultado),
                    HttpStatus.OK
                );
            }

            // Procesar correos con Gemini AI
            List<MensajeSunat> correosProcesados = mensajeSunatServicio.procesarNuevosCorreosConIA(correosNuevos);

            // Crear respuesta con estadísticas
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("correosNuevos", correosNuevos.size());
            resultado.put("correosProcesados", correosProcesados.size());
            
            // Estadísticas por clasificación
            long muyImportantes = correosProcesados.stream()
                .filter(c -> c.getNuUrgente() != null && c.getNuUrgente() == 1)
                .count();
            long importantes = correosProcesados.stream()
                .filter(c -> c.getNuDestacado() != null && c.getNuDestacado() == 1 && 
                           (c.getNuUrgente() == null || c.getNuUrgente() == 0))
                .count();
            long recurrentes = correosProcesados.size() - muyImportantes - importantes;
            
            resultado.put("clasificacion", Map.of(
                "muyImportantes", muyImportantes,
                "importantes", importantes,
                "recurrentes", recurrentes
            ));

            String mensaje = String.format(
                "Se procesaron %d correos nuevos: %d muy importantes, %d importantes, %d recurrentes. ¡Revisa las notificaciones en los logs!",
                correosProcesados.size(), muyImportantes, importantes, recurrentes
            );

            return new ResponseEntity<>(
                RespuestaControlador.exito(mensaje, resultado),
                HttpStatus.OK
            );

        } catch (Exception e) {
            RespuestaControlador<Map<String, Object>> respuestaError = new RespuestaControlador<>();
            respuestaError.setVcMensaje("Error al procesar correos nuevos: " + e.getMessage());
            respuestaError.setNuCodigo(500);
            respuestaError.setDatos(null);
            
            return new ResponseEntity<>(
                    respuestaError,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Activa manualmente la verificación de correos nuevos con notificaciones
     */
    @Operation(summary = "Verificar correos nuevos manualmente", 
               description = "Ejecuta manualmente la verificación de correos nuevos y los procesa con Gemini AI")
    @PostMapping("/verificar-correos-nuevos")
    public ResponseEntity<RespuestaControlador<String>> verificarCorreosNuevos(
            @Parameter(description = "RUC del contribuyente", required = true)
            @RequestParam("ruc") String ruc,
            @Parameter(description = "Límite de correos a verificar", required = false)
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        
        try {
            emailMonitoringService.verificarCorreosNuevosManuales(ruc, limit);
            
            return new ResponseEntity<>(
                RespuestaControlador.exito("Verificación manual completada. Revisa los logs para ver las notificaciones.", null),
                HttpStatus.OK
            );

        } catch (Exception e) {
            RespuestaControlador<String> respuestaError = new RespuestaControlador<>();
            respuestaError.setVcMensaje("Error en verificación manual: " + e.getMessage());
            respuestaError.setNuCodigo(500);
            respuestaError.setDatos(null);
            
            return new ResponseEntity<>(
                    respuestaError,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Simula la llegada de correos nuevos para testing
     */
    @Operation(summary = "Simular correos nuevos", 
               description = "Simula la llegada de correos nuevos para probar las notificaciones")
    @PostMapping("/simular-correos-nuevos")
    public ResponseEntity<RespuestaControlador<String>> simularCorreosNuevos() {
        
        try {
            emailMonitoringService.simularCorreosNuevos();
            
            return new ResponseEntity<>(
                RespuestaControlador.exito("Simulación completada. Revisa los logs para ver las notificaciones con clasificación y etiquetas.", null),
                HttpStatus.OK
            );

        } catch (Exception e) {
            RespuestaControlador<String> respuestaError = new RespuestaControlador<>();
            respuestaError.setVcMensaje("Error en simulación: " + e.getMessage());
            respuestaError.setNuCodigo(500);
            respuestaError.setDatos(null);
            
            return new ResponseEntity<>(
                    respuestaError,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Obtiene correos nuevos para notificaciones del frontend
     */
    @Operation(summary = "Obtener correos nuevos para notificaciones", 
               description = "Obtiene correos nuevos clasificados para mostrar notificaciones en el frontend")
    @GetMapping("/correos-nuevos-notificaciones")
    public ResponseEntity<RespuestaControlador<List<Map<String, Object>>>> obtenerCorreosNuevosParaNotificaciones(
            @Parameter(description = "RUC del contribuyente", required = true)
            @RequestParam("ruc") String ruc,
            @Parameter(description = "Timestamp desde cuando buscar (opcional)", required = false)
            @RequestParam(value = "desde", required = false) Long desde,
            @Parameter(description = "Límite de correos", required = false)
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        
        try {
            // Obtener correos recientes que han sido procesados recientemente
            List<MensajeSunat> correosRecientes = mensajeSunatServicio.obtenerTodosMensajes(ruc)
                .stream()
                .filter(correo -> !correo.getVcCodigoEtiqueta().equals("00")) // Solo procesados
                .limit(limit)
                .toList();

            // Convertir a formato para notificaciones
            List<Map<String, Object>> notificaciones = correosRecientes.stream()
                .map(correo -> {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("id", correo.getNuCodigoMensaje());
                    notif.put("asunto", correo.getVcAsunto());
                    notif.put("emisor", correo.getVcUsuarioEmisor());
                    notif.put("fechaPublica", correo.getVcFechaPublica());
                    notif.put("etiquetaCodigo", correo.getVcCodigoEtiqueta());
                    notif.put("etiquetaNombre", mensajeSunatServicio.obtenerDescripcionEtiqueta(correo.getVcCodigoEtiqueta()));
                    
                    // Determinar clasificación basada en flags
                    String clasificacion = "RECURRENTE";
                    String emoji = "🟢";
                    if (correo.getNuUrgente() != null && correo.getNuUrgente() == 1) {
                        clasificacion = "MUY IMPORTANTE";
                        emoji = "🔴";
                    } else if (correo.getNuDestacado() != null && correo.getNuDestacado() == 1) {
                        clasificacion = "IMPORTANTE";
                        emoji = "🟡";
                    }
                    
                    notif.put("clasificacion", clasificacion);
                    notif.put("emoji", emoji);
                    notif.put("urgente", correo.getNuUrgente());
                    notif.put("destacado", correo.getNuDestacado());
                    
                    return notif;
                })
                .toList();

            String mensaje = String.format("Se encontraron %d correos nuevos para notificaciones", notificaciones.size());
            
            return new ResponseEntity<>(
                RespuestaControlador.exito(mensaje, notificaciones),
                HttpStatus.OK
            );

        } catch (Exception e) {
            RespuestaControlador<List<Map<String, Object>>> respuestaError = new RespuestaControlador<>();
            respuestaError.setVcMensaje("Error al obtener correos para notificaciones: " + e.getMessage());
            respuestaError.setNuCodigo(500);
            respuestaError.setDatos(null);
            
            return new ResponseEntity<>(
                    respuestaError,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Obtiene estadísticas de correos extraídos y clasificados para mostrar después del login
     */
    @Operation(summary = "Estadísticas post-login", 
               description = "Obtiene resumen de correos extraídos y clasificados por etiquetas para mostrar después del login del usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "400", description = "RUC inválido"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/estadisticas-post-login")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerEstadisticasPostLogin(
            @Parameter(description = "RUC del contribuyente", required = true)
            @RequestParam("ruc") String ruc,
            @Parameter(description = "Incluir solo correos nuevos desde el último login", required = false)
            @RequestParam(value = "soloNuevos", defaultValue = "false") Boolean soloNuevos) {
        
        try {
            // Obtener todos los correos del RUC
            List<MensajeSunat> todosLosCorreos = mensajeSunatServicio.obtenerTodosMensajes(ruc);
            
            // Filtrar correos nuevos si se solicita
            List<MensajeSunat> correosParaAnalizar;
            if (soloNuevos) {
                // Obtener correos de las últimas 24 horas o desde la última verificación
                long tiempoLimite = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 horas
                correosParaAnalizar = todosLosCorreos.stream()
                    .filter(correo -> {
                        // Convertir fecha string a timestamp para comparar
                        try {
                            // Asumir formato de fecha similar a "2024-06-19"
                            String fechaEnvio = correo.getVcFechaEnvio();
                            if (fechaEnvio != null && !fechaEnvio.isEmpty()) {
                                return true; // Por ahora incluir todos, se puede refinar
                            }
                            return false;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .toList();
            } else {
                correosParaAnalizar = todosLosCorreos;
            }
            
            // Contar total de correos extraídos
            int totalExtraidos = correosParaAnalizar.size();
            
            // Contar por clasificación
            Map<String, Integer> clasificaciones = new HashMap<>();
            clasificaciones.put("MUY IMPORTANTE", 0);
            clasificaciones.put("IMPORTANTE", 0);
            clasificaciones.put("RECURRENTE", 0);
            clasificaciones.put("SIN CLASIFICAR", 0);
            
            // Contar por etiquetas
            Map<String, Object> estadisticasEtiquetas = new HashMap<>();
            Map<String, Integer> conteoEtiquetas = new HashMap<>();
            Map<String, String> nombresEtiquetas = new HashMap<>();
            
            System.out.println("🔍 DEBUG: Procesando " + correosParaAnalizar.size() + " correos para RUC: " + ruc);
            
            for (MensajeSunat correo : correosParaAnalizar) {
                // Contar clasificaciones
                String clasificacion = correo.getClasificacion();
                if (clasificacion != null && !clasificacion.isEmpty()) {
                    clasificaciones.put(clasificacion, clasificaciones.getOrDefault(clasificacion, 0) + 1);
                } else {
                    clasificaciones.put("SIN CLASIFICAR", clasificaciones.get("SIN CLASIFICAR") + 1);
                }
                
                // Contar etiquetas
                String codigoEtiqueta = correo.getVcCodigoEtiqueta();
                
                if (codigoEtiqueta != null && !codigoEtiqueta.isEmpty() && !"00".equals(codigoEtiqueta)) {
                    System.out.println("🏷️ DEBUG: Encontrada etiqueta: " + codigoEtiqueta + " en correo: " + correo.getVcAsunto());
                    conteoEtiquetas.put(codigoEtiqueta, conteoEtiquetas.getOrDefault(codigoEtiqueta, 0) + 1);
                    
                    // Obtener nombre de etiqueta desde el servicio solo si no lo tenemos ya
                    if (!nombresEtiquetas.containsKey(codigoEtiqueta)) {
                        try {
                            Optional<Etiqueta> etiquetaOpt = etiquetaServicio.obtenerEtiquetaPorCodigo(codigoEtiqueta);
                            if (etiquetaOpt.isPresent()) {
                                String nombreEtiqueta = etiquetaOpt.get().getVcNombre();
                                nombresEtiquetas.put(codigoEtiqueta, nombreEtiqueta);
                                System.out.println("✅ DEBUG: Etiqueta " + codigoEtiqueta + " -> " + nombreEtiqueta);
                            } else {
                                nombresEtiquetas.put(codigoEtiqueta, "Etiqueta " + codigoEtiqueta);
                                System.out.println("⚠️ DEBUG: Etiqueta " + codigoEtiqueta + " no encontrada en BD, usando nombre genérico");
                            }
                        } catch (Exception e) {
                            // Si no se encuentra la etiqueta, usar el código como nombre
                            nombresEtiquetas.put(codigoEtiqueta, "Etiqueta " + codigoEtiqueta);
                            System.out.println("❌ DEBUG: Error al buscar etiqueta " + codigoEtiqueta + ": " + e.getMessage());
                        }
                    }
                } else if (codigoEtiqueta != null) {
                    System.out.println("🔄 DEBUG: Correo sin etiqueta específica (código: " + codigoEtiqueta + "): " + correo.getVcAsunto());
                }
            }
            
            System.out.println("📊 DEBUG: Resumen de etiquetas encontradas:");
            conteoEtiquetas.forEach((codigo, cantidad) -> {
                String nombre = nombresEtiquetas.get(codigo);
                System.out.println("  - " + codigo + " (" + nombre + "): " + cantidad + " correos");
            });
            
            // Preparar respuesta
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalExtraidos", totalExtraidos);
            estadisticas.put("clasificaciones", clasificaciones);
            estadisticas.put("etiquetas", conteoEtiquetas);
            estadisticas.put("nombresEtiquetas", nombresEtiquetas);
            estadisticas.put("ruc", ruc);
            estadisticas.put("fechaConsulta", java.time.LocalDateTime.now().toString());
            estadisticas.put("soloNuevos", soloNuevos);
            
            // Crear mensaje personalizado
            StringBuilder mensaje = new StringBuilder();
            if (totalExtraidos > 0) {
                mensaje.append("Se han extraído un total de ").append(totalExtraidos).append(" correos. ");
                
                // Agregar detalle de clasificaciones
                if (clasificaciones.get("MUY IMPORTANTE") > 0) {
                    mensaje.append(clasificaciones.get("MUY IMPORTANTE")).append(" muy importantes, ");
                }
                if (clasificaciones.get("IMPORTANTE") > 0) {
                    mensaje.append(clasificaciones.get("IMPORTANTE")).append(" importantes, ");
                }
                if (clasificaciones.get("RECURRENTE") > 0) {
                    mensaje.append(clasificaciones.get("RECURRENTE")).append(" recurrentes");
                }
                
                // Limpiar coma final
                if (mensaje.toString().endsWith(", ")) {
                    mensaje.setLength(mensaje.length() - 2);
                }
            } else {
                mensaje.append("No se encontraron correos extraídos para el RUC proporcionado");
            }
            
            return ResponseEntity.ok(
                new RespuestaControlador<>(mensaje.toString(), 200, estadisticas, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener estadísticas: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * Obtiene el estado actual del sistema de rate limiting de Gemini AI
     */
    @Operation(summary = "Estado de Gemini AI", 
               description = "Obtiene información sobre el estado del rate limiter y circuit breaker de Gemini AI")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/gemini/status")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerEstadoGemini() {
        try {
            GeminiAIService.RateLimiterStatus status = geminiAIService.getRateLimiterStatus();
            
            Map<String, Object> estadoDetallado = new HashMap<>();
            estadoDetallado.put("enabled", status.isEnabled());
            estadoDetallado.put("requestsInLastMinute", status.getRequestsInLastMinute());
            estadoDetallado.put("maxRequestsPerMinute", status.getMaxRequestsPerMinute());
            estadoDetallado.put("availableRequests", status.getMaxRequestsPerMinute() - status.getRequestsInLastMinute());
            estadoDetallado.put("circuitBreakerOpen", status.isCircuitBreakerOpen());
            estadoDetallado.put("consecutiveFailures", status.getConsecutiveFailures());
            
            if (status.getCircuitBreakerOpenTime() != null) {
                estadoDetallado.put("circuitBreakerOpenSince", status.getCircuitBreakerOpenTime().toString());
            }
            
            // Determinar estado general
            String estadoGeneral;
            if (!status.isEnabled()) {
                estadoGeneral = "DESHABILITADO";
            } else if (status.isCircuitBreakerOpen()) {
                estadoGeneral = "CIRCUIT_BREAKER_ABIERTO";
            } else if (status.getRequestsInLastMinute() >= status.getMaxRequestsPerMinute()) {
                estadoGeneral = "LIMITE_ALCANZADO";
            } else if (status.getRequestsInLastMinute() >= status.getMaxRequestsPerMinute() * 0.8) {
                estadoGeneral = "LIMITE_PROXIMO";
            } else {
                estadoGeneral = "OPERATIVO";
            }
            
            estadoDetallado.put("status", estadoGeneral);
            
            return ResponseEntity.ok(
                new RespuestaControlador<>("Estado de Gemini AI obtenido exitosamente", 200, estadoDetallado, null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaControlador<>("Error al obtener estado de Gemini AI: " + e.getMessage(), 500, null, null)
            );
        }
    }

    /**
     * ENDPOINT DE DEBUGGING - Ver todos los mensajes con sus RUCs
     */
    @GetMapping("/debug/todos-mensajes")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> debugTodosMensajes() {
        List<MensajeSunat> todosMensajes = mensajeSunatServicio.obtenerTodosMensajes("");
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("total_mensajes", todosMensajes.size());
        debug.put("mensajes", todosMensajes.stream().limit(10).map(m -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", m.getNuCodigoMensaje());
            info.put("asunto", m.getVcAsunto());
            info.put("ruc", m.getVcNumeroRuc());
            info.put("clasificacion", m.getClasificacion());
            info.put("etiqueta", m.getVcCodigoEtiqueta());
            return info;
        }).toList());
        
        // Agrupar por RUC
        Map<String, Long> porRuc = todosMensajes.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                m -> m.getVcNumeroRuc() != null ? m.getVcNumeroRuc() : "NULL",
                java.util.stream.Collectors.counting()
            ));
        debug.put("por_ruc", porRuc);
        
        return new ResponseEntity<>(
                RespuestaControlador.exito(debug),
                HttpStatus.OK
        );
    }

    /**
     * ENDPOINT PARA PROCESAR TODOS LOS MENSAJES SIN CLASIFICACIÓN CON IA
     */
    @PostMapping("/debug/clasificar-todos-con-ia")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> procesarMensajesSinClasificacionConIA() {
        try {
            // Obtener todos los mensajes sin clasificación
            List<MensajeSunat> todosMensajes = mensajeSunatServicio.obtenerTodosMensajes("");
            List<MensajeSunat> sinClasificacion = todosMensajes.stream()
                .filter(m -> m.getClasificacion() == null || m.getClasificacion().trim().isEmpty())
                .toList();
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("total_mensajes", todosMensajes.size());
            resultado.put("sin_clasificacion", sinClasificacion.size());
            
            if (sinClasificacion.isEmpty()) {
                resultado.put("mensaje", "No hay mensajes sin clasificación");
                return new ResponseEntity<>(
                    RespuestaControlador.exito(resultado),
                    HttpStatus.OK
                );
            }
            
            // Procesar con IA (máximo 50 por vez para no sobrecargar)
            List<MensajeSunat> aProcesar = sinClasificacion.stream()
                .limit(50)
                .toList();
            
            int procesados = 0;
            int errores = 0;
            
            for (MensajeSunat mensaje : aProcesar) {
                try {
                    MensajeSunat procesado = mensajeSunatServicio.procesarCorreoIndividualConIA(mensaje);
                    if (procesado.getClasificacion() != null) {
                        procesados++;
                    }
                } catch (Exception e) {
                    errores++;
                    System.err.println("Error procesando mensaje " + mensaje.getNuCodigoMensaje() + ": " + e.getMessage());
                }
            }
            
            resultado.put("procesados_exitosamente", procesados);
            resultado.put("errores", errores);
            resultado.put("mensaje", "Procesamiento completado. Procesados: " + procesados + ", Errores: " + errores);
            
            return new ResponseEntity<>(
                RespuestaControlador.exito(resultado),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al procesar mensajes: " + e.getMessage());
            
            RespuestaControlador<Map<String, Object>> respuestaError = new RespuestaControlador<>();
            respuestaError.setVcMensaje("Error al procesar mensajes: " + e.getMessage());
            respuestaError.setNuCodigo(500);
            respuestaError.setDatos(error);
            
            return new ResponseEntity<>(
                respuestaError,
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * ENDPOINT PARA VER CÓMO LA IA CLASIFICA MENSAJES - CON DETALLES
     */
    @GetMapping("/debug/ver-clasificacion-ia")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> verClasificacionIA(
            @RequestParam(defaultValue = "10") int limite) {
        try {
            
            List<MensajeSunat> mensajesClasificados = mensajeSunatServicio.obtenerTodosMensajes("")
                .stream()
                .filter(m -> m.getClasificacion() != null)
                .limit(limite)
                .collect(java.util.stream.Collectors.toList());
            
            List<MensajeSunat> mensajesSinClasificar = mensajeSunatServicio.obtenerTodosMensajes("")
                .stream()
                .filter(m -> m.getClasificacion() == null)
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> resultado = new HashMap<>();
            
            // Mensajes ya clasificados con detalles
            resultado.put("mensajes_clasificados", mensajesClasificados.stream().map(m -> {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("id", m.getNuCodigoMensaje());
                detalle.put("asunto_completo", m.getVcAsunto());
                detalle.put("clasificacion_ia", m.getClasificacion());
                detalle.put("etiqueta", m.getVcCodigoEtiqueta());
                detalle.put("ruc", m.getVcNumeroRuc());
                return detalle;
            }).collect(java.util.stream.Collectors.toList()));
            
            // Mensajes sin clasificar para entender qué falta
            resultado.put("mensajes_sin_clasificar", mensajesSinClasificar.stream().map(m -> {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("id", m.getNuCodigoMensaje());
                detalle.put("asunto_completo", m.getVcAsunto());
                detalle.put("etiqueta", m.getVcCodigoEtiqueta());
                return detalle;
            }).collect(java.util.stream.Collectors.toList()));
            
            // Estadísticas de clasificación
            Map<String, Long> estadisticas = mensajeSunatServicio.obtenerTodosMensajes("").stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    m -> m.getClasificacion() != null ? m.getClasificacion() : "SIN_CLASIFICAR",
                    java.util.stream.Collectors.counting()
                ));
            resultado.put("estadisticas_clasificacion", estadisticas);
            
            resultado.put("total_clasificados", mensajesClasificados.size());
            resultado.put("total_sin_clasificar", mensajesSinClasificar.size());
            
            return new ResponseEntity<>(
                RespuestaControlador.exito(resultado),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener detalles de clasificación: " + e.getMessage());
            
            RespuestaControlador<Map<String, Object>> respuestaError = new RespuestaControlador<>();
            respuestaError.setVcMensaje("Error al obtener detalles: " + e.getMessage());
            respuestaError.setNuCodigo(500);
            respuestaError.setDatos(error);
            
            return new ResponseEntity<>(
                respuestaError,
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * ENDPOINT PARA CLASIFICAR AUTOMATICAMENTE TODOS LOS CORREOS BASADO EN PALABRAS CLAVE
     */
    @PostMapping("/clasificar-automatico")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> clasificarAutomatico() {
        try {
            // Obtener todos los mensajes extraidos
            List<MensajeSunat> todosMensajes = mensajeSunatServicio.obtenerTodosMensajes("");
            
            Map<String, Object> resultado = new HashMap<>();
            Map<String, Integer> contadores = new HashMap<>();
            contadores.put("MUY_IMPORTANTE", 0);
            contadores.put("IMPORTANTE", 0);
            contadores.put("INFORMATIVO", 0);
            contadores.put("RECURRENTE", 0);
            contadores.put("NO_CLASIFICADOS", 0);
            
            int procesados = 0;
            int exitosos = 0;
            
            for (MensajeSunat mensaje : todosMensajes) {
                try {
                    procesados++;
                    String asunto = mensaje.getVcAsunto();
                    
                    if (asunto == null || asunto.trim().isEmpty()) {
                        contadores.put("NO_CLASIFICADOS", contadores.get("NO_CLASIFICADOS") + 1);
                        continue;
                    }
                    
                    // Clasificar basado en palabras clave
                    String clasificacion = clasificarPorPalabrasClave(asunto);
                    
                    // Buscar la etiqueta correspondiente
                    String codigoEtiqueta = obtenerCodigoEtiqueta(clasificacion);
                    
                    // Actualizar el mensaje con la nueva etiqueta usando el servicio
                    mensajeSunatServicio.actualizarEtiqueta(mensaje.getNuCodigoMensaje(), codigoEtiqueta);
                    
                    contadores.put(clasificacion, contadores.get(clasificacion) + 1);
                    exitosos++;
                    
                } catch (Exception e) {
                    System.err.println("Error al procesar mensaje ID " + mensaje.getNuCodigoMensaje() + ": " + e.getMessage());
                    contadores.put("NO_CLASIFICADOS", contadores.get("NO_CLASIFICADOS") + 1);
                }
            }
            
            resultado.put("total_procesados", procesados);
            resultado.put("exitosos", exitosos);
            resultado.put("errores", procesados - exitosos);
            resultado.put("contadores_por_clasificacion", contadores);
            
            String mensaje = String.format("Clasificación automática completada: %d/%d correos procesados exitosamente", 
                                         exitosos, procesados);
            
            return new ResponseEntity<>(
                RespuestaControlador.exito(mensaje, resultado),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en clasificación automática: " + e.getMessage());
            
            RespuestaControlador<Map<String, Object>> respuestaError = new RespuestaControlador<>();
            respuestaError.setVcMensaje("Error en clasificación automática: " + e.getMessage());
            respuestaError.setNuCodigo(500);
            respuestaError.setDatos(error);
            
            return new ResponseEntity<>(
                respuestaError,
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Clasifica un correo basado en palabras clave del asunto
     */
    private String clasificarPorPalabrasClave(String asunto) {
        String asuntoLower = asunto.toLowerCase();
        
        // MUY IMPORTANTE - Pagos, ordenes, vencimientos críticos
        if (asuntoLower.contains("orden de pago") || 
            asuntoLower.contains("pago de") ||
            asuntoLower.contains("vencimiento") ||
            asuntoLower.contains("vence") ||
            asuntoLower.contains("deuda") ||
            asuntoLower.contains("cobranza") ||
            asuntoLower.contains("intimación") ||
            asuntoLower.contains("embargo") ||
            asuntoLower.contains("multa") ||
            asuntoLower.contains("sanción") ||
            asuntoLower.contains("requerimiento") ||
            asuntoLower.contains("urgente")) {
            return "MUY_IMPORTANTE";
        }
        
        // IMPORTANTE - Declaraciones, autorizaciones, certificados
        if (asuntoLower.contains("declaración") ||
            asuntoLower.contains("declarar") ||
            asuntoLower.contains("autorización") ||
            asuntoLower.contains("certificado") ||
            asuntoLower.contains("comprobante") ||
            asuntoLower.contains("validación") ||
            asuntoLower.contains("verificación") ||
            asuntoLower.contains("constancia") ||
            asuntoLower.contains("resolución") ||
            asuntoLower.contains("notificación") ||
            asuntoLower.contains("renta") ||
            asuntoLower.contains("igv") ||
            asuntoLower.contains("essalud") ||
            asuntoLower.contains("afp")) {
            return "IMPORTANTE";
        }
        
        // INFORMATIVO - Formularios, cambios, actualizaciones
        if (asuntoLower.contains("formulario") ||
            asuntoLower.contains("cambio") ||
            asuntoLower.contains("modificación") ||
            asuntoLower.contains("actualización") ||
            asuntoLower.contains("nuevo") ||
            asuntoLower.contains("nueva") ||
            asuntoLower.contains("información") ||
            asuntoLower.contains("comunicado") ||
            asuntoLower.contains("aviso") ||
            asuntoLower.contains("procedimiento") ||
            asuntoLower.contains("sistema") ||
            asuntoLower.contains("plataforma") ||
            asuntoLower.contains("servicio")) {
            return "INFORMATIVO";
        }
        
        // Si no coincide con ninguna categoría específica, es RECURRENTE
        return "RECURRENTE";
    }

    /**
     * Obtiene el código de etiqueta correspondiente a la clasificación
     */
    private String obtenerCodigoEtiqueta(String clasificacion) {
        switch (clasificacion) {
            case "MUY_IMPORTANTE":
                return "10"; // Etiqueta roja
            case "IMPORTANTE":
                return "11"; // Etiqueta amarilla  
            case "INFORMATIVO":
                return "13"; // Etiqueta azul
            case "RECURRENTE":
            default:
                return "14"; // Etiqueta verde
        }
    }

    /**
     * ENDPOINT PARA VER SOLO LOS ASUNTOS DE TODOS LOS CORREOS
     */
    @GetMapping("/debug/solo-asuntos")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> obtenerSoloAsuntos(
            @RequestParam(defaultValue = "0") int limite) {
        try {
            
            List<MensajeSunat> mensajes = mensajeSunatServicio.obtenerTodosMensajes("");
            
            // Si limite es 0, obtener todos los mensajes
            if (limite > 0) {
                mensajes = mensajes.stream()
                    .limit(limite)
                    .collect(java.util.stream.Collectors.toList());
            }
            
            Map<String, Object> resultado = new HashMap<>();
            
            // Solo los asuntos sin metadata
            List<String> soloAsuntos = mensajes.stream()
                .map(m -> m.getVcAsunto())
                .filter(asunto -> asunto != null && !asunto.trim().isEmpty())
                .collect(java.util.stream.Collectors.toList());
            
            // Asuntos con ID para referencia
            List<Map<String, Object>> asuntosConId = mensajes.stream().map(m -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", m.getNuCodigoMensaje());
                item.put("asunto", m.getVcAsunto());
                return item;
            }).collect(java.util.stream.Collectors.toList());
            
            resultado.put("solo_asuntos", soloAsuntos);
            resultado.put("asuntos_con_id", asuntosConId);
            resultado.put("total", soloAsuntos.size());
            resultado.put("limite_aplicado", limite > 0 ? limite : "SIN LIMITE - TODOS");
            
            return new ResponseEntity<>(
                RespuestaControlador.exito(resultado),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener asuntos: " + e.getMessage());
            
            RespuestaControlador<Map<String, Object>> respuestaError = new RespuestaControlador<>();
            respuestaError.setVcMensaje("Error: " + e.getMessage());
            respuestaError.setNuCodigo(500);
            respuestaError.setDatos(error);
            
            return new ResponseEntity<>(
                respuestaError,
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}