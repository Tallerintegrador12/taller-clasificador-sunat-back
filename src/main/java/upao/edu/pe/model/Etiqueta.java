package upao.edu.pe.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_ETIQUETA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NU_ID_ETIQUETA")
    private Long nuIdEtiqueta;

    @Column(name = "VC_NOMBRE", length = 100, nullable = false)
    private String vcNombre;

    @Column(name = "VC_COLOR", length = 20)
    private String vcColor;

    @Column(name = "VC_CODIGO", length = 20, unique = true, nullable = false)
    private String vcCodigo;
}