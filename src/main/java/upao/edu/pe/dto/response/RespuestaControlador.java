package upao.edu.pe.dto.response;

import lombok.*;

import java.util.List;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaControlador<T> {

    private String vcMensaje;
    private Integer nuCodigo;
    private T datos;
    private List<String> vcErrores;


    public void setNuCodigo(Integer nuCodigo) {
        this.nuCodigo = nuCodigo;
    }

    public void setVcMensaje(String vcMensaje) {
        this.vcMensaje = vcMensaje;
    }

    public void setDatos(T datos) {
        this.datos = datos;
    }

    public void setVcErrores(List<String> vcErrores) {
        this.vcErrores = vcErrores;
    }


    public String getVcMensaje() {
        return vcMensaje;
    }

    public Integer getNuCodigo() {
        return nuCodigo;
    }

    public T getDatos() {
        return datos;
    }

    public List<String> getVcErrores() {
        return vcErrores;
    }


    // Para cuando solo tienes datos
    public static <T> RespuestaControlador<T> exito(T datos) {
        RespuestaControlador<T> respuesta = new RespuestaControlador<>();
        respuesta.setVcMensaje("Operación exitosa");
        respuesta.setNuCodigo(200);
        respuesta.setDatos(datos);
        return respuesta;
    }

    // Para cuando quieres personalizar el mensaje
    public static <T> RespuestaControlador<T> exito(String mensaje, T datos) {
        RespuestaControlador<T> respuesta = new RespuestaControlador<>();
        respuesta.setVcMensaje(mensaje);
        respuesta.setNuCodigo(200);
        respuesta.setDatos(datos);
        return respuesta;
    }
}