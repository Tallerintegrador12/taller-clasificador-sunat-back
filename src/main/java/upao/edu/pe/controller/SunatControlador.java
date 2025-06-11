package upao.edu.pe.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upao.edu.pe.dto.response.RespuestaControlador;
import upao.edu.pe.model.MensajeSunat;
import upao.edu.pe.service.EtiquetaServicio;
import upao.edu.pe.service.MensajeSunatServicio;
import upao.edu.pe.service.SunatNotificacionService;
import upao.edu.pe.service.SunatServicio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sunat")
public class SunatControlador {

    @Autowired
    private SunatServicio sunatServicio;

    @Autowired
    private MensajeSunatServicio mensajeSunatServicio;

    @Autowired
    private SunatNotificacionService sunatNotificacionService;

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
    @GetMapping("/mensajes")
    public ResponseEntity<RespuestaControlador<List<MensajeSunat>>> obtenerMensajes(String vc_numero_ruc) {
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
}