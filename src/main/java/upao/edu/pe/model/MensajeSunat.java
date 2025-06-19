package upao.edu.pe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_MENSAJE_SUNAT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeSunat {

    @Id
    @Column(name = "NU_CODIGO_MENSAJE")
    private Long nuCodigoMensaje;

    @Column(name = "NU_PAGINA")
    private Integer nuPagina;

    @Column(name = "NU_ESTADO")
    private Integer nuEstado;

    @Column(name = "NU_DESTACADO")
    private Integer nuDestacado;

    @Column(name = "NU_URGENTE")
    private Integer nuUrgente;

    @Column(name = "DT_FECHA_VIGENCIA")
    private LocalDateTime dtFechaVigencia;

    @Column(name = "NU_TIPO_MENSAJE")
    private Integer nuTipoMensaje;

    @Column(name = "VC_ASUNTO", length = 255)
    private String vcAsunto;

    @Column(name = "VC_FECHA_ENVIO", length = 20)
    private String vcFechaEnvio;

    @Column(name = "VC_FECHA_PUBLICA", length = 30)
    private String vcFechaPublica;

    @Column(name = "VC_USUARIO_EMISOR", length = 50)
    private String vcUsuarioEmisor;

    @Column(name = "NU_INDICADOR_TEXTO")
    private Integer nuIndicadorTexto;

    @Column(name = "NU_TIPO_GENERADOR")
    private Integer nuTipoGenerador;

    @Column(name = "VC_CODIGO_DEPENDENCIA", length = 50)
    private String vcCodigoDependencia;

    @Column(name = "NU_AVISO")
    private Integer nuAviso;

    @Column(name = "NU_CANTIDAD_ARCHIVOS")
    private Integer nuCantidadArchivos;

    @Column(name = "VC_CODIGO_ETIQUETA", length = 20)
    private String vcCodigoEtiqueta;

    @Column(name = "NU_MENSAJE")
    private Integer nuMensaje;

    @Column(name = "VC_CODIGO_CARPETA", length = 50)
    private String vcCodigoCarpeta;

    @Column(name = "VC_NUMERO_RUC", length = 20)
    private String vcNumeroRuc;

    // Nuevas columnas
    @Column(name = "NU_LEIDO")
    @Builder.Default
    private Integer nuLeido = 0; // 1: leído, 0: no leído

    @Column(name = "NU_ARCHIVADO")
    @Builder.Default
    private Integer nuArchivado = 0; // 1: archivado, 0: no archivado

    // Nueva columna para la clasificación
    @Column(name = "CLASIFICACION", length = 30)
    private String clasificacion;
}