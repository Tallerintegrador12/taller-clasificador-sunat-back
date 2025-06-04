package upao.edu.pe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "T_DETALLE_MENSAJE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MENSAJE")
    private Long idMensaje;

    @Column(name = "VC_COD_MENSAJE", unique = true)
    private String codMensaje;

    @Column(name = "VC_DETALLE_MENSAJE", length = 10000)
    private String msjMensaje;

    @Column(name = "VC_COD_USUARIO")
    private String codUsuario;

    @Column(name = "VC_NOMB_USUARIO")
    private String nombUsuario;

    @Column(name = "VC_SISTEMA")
    private String sistema;

    @Column(name = "VC_ID_ARCHIVO")
    private String idArchivo;

    @Column(name = "VC_DEPENDENCIA")
    private String dependencia;

    @Column(name = "VC_NUM_DOC")
    private String numDoc;

    @Column(name = "VC_DES_TIP_DOC", length = 500)
    private String desTipDoc;

    @Column(name = "VC_ID_ANEXO")
    private String idAnexo;

    @Column(name = "VC_RAZON_SOCIAL")
    private String razonSocial;

    @Column(name = "VC_NOMBRE")
    private String nombre;

    @Column(name = "VC_NUMRUC")
    private String numruc;

    @Column(name = "VC_URL", length = 1000)
    private String url;

    @Column(name = "DT_CREACION")
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "detalleNotificacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AttachmentDetalle> listAttach;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
