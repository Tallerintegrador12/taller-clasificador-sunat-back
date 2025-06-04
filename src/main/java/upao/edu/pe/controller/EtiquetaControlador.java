package upao.edu.pe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upao.edu.pe.dto.response.RespuestaControlador;
import upao.edu.pe.model.Etiqueta;
import upao.edu.pe.service.EtiquetaServicio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/etiquetas")
public class EtiquetaControlador {

    @Autowired
    private EtiquetaServicio etiquetaServicio;

    /**
     * Crea una nueva etiqueta
     */
    @PostMapping
    public ResponseEntity<RespuestaControlador<Etiqueta>> crearEtiqueta(
            @RequestParam String vcNombre,
            @RequestParam(required = false) String vcColor) {

        Etiqueta etiqueta = etiquetaServicio.crearEtiqueta(vcNombre, vcColor);

        return new ResponseEntity<>(
                RespuestaControlador.exito("Etiqueta creada exitosamente", etiqueta),
                HttpStatus.CREATED
        );
    }

    /**
     * Obtiene todas las etiquetas
     */
    @GetMapping
    public ResponseEntity<RespuestaControlador<List<Etiqueta>>> obtenerTodasLasEtiquetas() {
        List<Etiqueta> etiquetas = etiquetaServicio.obtenerTodasLasEtiquetas();

        return new ResponseEntity<>(
                RespuestaControlador.exito("Listado de etiquetas", etiquetas),
                HttpStatus.OK
        );
    }

    /**
     * Obtiene una etiqueta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaControlador<Etiqueta>> obtenerEtiquetaPorId(@PathVariable Long id) {
        Etiqueta etiqueta = etiquetaServicio.obtenerEtiquetaPorId(id);

        return new ResponseEntity<>(
                RespuestaControlador.exito("Etiqueta encontrada", etiqueta),
                HttpStatus.OK
        );
    }

    /**
     * Obtiene una etiqueta por código
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<RespuestaControlador<Etiqueta>> obtenerEtiquetaPorCodigo(@PathVariable String codigo) {
        Optional<Etiqueta> etiqueta = etiquetaServicio.obtenerEtiquetaPorCodigo(codigo);

        if (etiqueta.isPresent()) {
            return new ResponseEntity<>(
                    RespuestaControlador.exito("Etiqueta encontrada", etiqueta.get()),
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                    RespuestaControlador.exito("Etiqueta no encontrada", null),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    /**
     * Actualiza una etiqueta existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaControlador<Etiqueta>> actualizarEtiqueta(
            @PathVariable Long id,
            @RequestParam(required = false) String vcNombre,
            @RequestParam(required = false) String vcColor) {

        Etiqueta etiqueta = etiquetaServicio.actualizarEtiqueta(id, vcNombre, vcColor);

        return new ResponseEntity<>(
                RespuestaControlador.exito("Etiqueta actualizada exitosamente", etiqueta),
                HttpStatus.OK
        );
    }

    /**
     * Elimina una etiqueta y reasigna todos los mensajes a "no etiquetados"
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaControlador<Map<String, Object>>> eliminarEtiqueta(@PathVariable Long id) {
        int mensajesActualizados = etiquetaServicio.eliminarEtiqueta(id);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("mensajesReasignados", mensajesActualizados);
        resultado.put("mensaje", "Etiqueta eliminada y " + mensajesActualizados + " mensajes reasignados a 'No etiquetados'");

        return new ResponseEntity<>(
                RespuestaControlador.exito("Etiqueta eliminada exitosamente", resultado),
                HttpStatus.OK
        );
    }
}