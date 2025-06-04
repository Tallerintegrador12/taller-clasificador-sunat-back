package upao.edu.pe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "T_ATTACHMENT_DETALLE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ATTACHMENT")
    private Long idAttachment;

    @Column(name = "VC_COD_MENSAJE")
    private String codMensaje;

    @Column(name = "NM_COD_ARCHIVO")
    private Long codArchivo;

    @Column(name = "VC_NOM_ARCHIVO", length = 500)
    private String nomArchivo;

    @Column(name = "VC_NOM_ADJUNTO")
    private String nomAdjunto;

    @Column(name = "NM_CNT_TAMARCH")
    private Long cntTamarch;

    @Column(name = "NM_NUM_ID")
    private Long numId;

    @Column(name = "VC_IND_MENSAJE")
    private String indMensaje;

    @Column(name = "VC_NUM_ECM")
    private String numEcm;

    @Column(name = "VC_TAMANO_ARCHIVO_FORMAT")
    private String tamanoArchivoFormat;

    @Column(name = "VC_URL", length = 1000)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MENSAJE_FK")
    @JsonIgnore
    private DetalleNotificacion detalleNotificacion;
}