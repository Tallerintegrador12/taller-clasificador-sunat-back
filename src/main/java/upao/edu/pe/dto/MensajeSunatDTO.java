package upao.edu.pe.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
public class MensajeSunatDTO {

    @JsonProperty("codMensaje")
    private Long nuCodigoMensaje;

    @JsonProperty("numPag")
    private Integer nuPagina;

    @JsonProperty("indEstado")
    private Integer nuEstado;

    @JsonProperty("indDesta")
    private Integer nuDestacado;

    @JsonProperty("indUrg")
    private Integer nuUrgente;

    @JsonProperty("fecVigencia")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.S")
    private String dtFechaVigencia;

    @JsonProperty("indTipmsj")
    private Integer nuTipoMensaje;

    @JsonProperty("desAsunto")
    private String vcAsunto;

    @JsonProperty("fecEnvio")
    private String vcFechaEnvio;

    @JsonProperty("fecPublica")
    private String vcFechaPublica;

    @JsonProperty("codUsremisor")
    private String vcUsuarioEmisor;

    @JsonProperty("indTexto")
    private Integer nuIndicadorTexto;

    @JsonProperty("indTipgen")
    private Integer nuTipoGenerador;

    @JsonProperty("codDepen")
    private String vcCodigoDependencia;

    @JsonProperty("indAviso")
    private Integer nuAviso;

    @JsonProperty("cantidadArchAdj")
    private Integer nuCantidadArchivos;

    @JsonProperty("codEtiqueta")
    private String vcCodigoEtiqueta;

    @JsonProperty("indMensaje")
    private Integer nuMensaje;

    @JsonProperty("codCarpeta")
    private String vcCodigoCarpeta;

    @JsonProperty("numRuc")
    private String vcNumeroRuc;

    public Integer getNuPagina() {
        return nuPagina;
    }

    public void setNuPagina(Integer nuPagina) {
        this.nuPagina = nuPagina;
    }

    public Integer getNuEstado() {
        return nuEstado;
    }

    public void setNuEstado(Integer nuEstado) {
        this.nuEstado = nuEstado;
    }

    public Integer getNuDestacado() {
        return nuDestacado;
    }

    public void setNuDestacado(Integer nuDestacado) {
        this.nuDestacado = nuDestacado;
    }

    public Integer getNuUrgente() {
        return nuUrgente;
    }

    public void setNuUrgente(Integer nuUrgente) {
        this.nuUrgente = nuUrgente;
    }

    public String getDtFechaVigencia() {
        return dtFechaVigencia;
    }

    public void setDtFechaVigencia(String dtFechaVigencia) {
        this.dtFechaVigencia = dtFechaVigencia;
    }

    public Integer getNuTipoMensaje() {
        return nuTipoMensaje;
    }

    public void setNuTipoMensaje(Integer nuTipoMensaje) {
        this.nuTipoMensaje = nuTipoMensaje;
    }

    public String getVcAsunto() {
        return vcAsunto;
    }

    public void setVcAsunto(String vcAsunto) {
        this.vcAsunto = vcAsunto;
    }

    public String getVcFechaEnvio() {
        return vcFechaEnvio;
    }

    public void setVcFechaEnvio(String vcFechaEnvio) {
        this.vcFechaEnvio = vcFechaEnvio;
    }

    public String getVcFechaPublica() {
        return vcFechaPublica;
    }

    public void setVcFechaPublica(String vcFechaPublica) {
        this.vcFechaPublica = vcFechaPublica;
    }

    public String getVcUsuarioEmisor() {
        return vcUsuarioEmisor;
    }

    public void setVcUsuarioEmisor(String vcUsuarioEmisor) {
        this.vcUsuarioEmisor = vcUsuarioEmisor;
    }

    public Integer getNuIndicadorTexto() {
        return nuIndicadorTexto;
    }

    public void setNuIndicadorTexto(Integer nuIndicadorTexto) {
        this.nuIndicadorTexto = nuIndicadorTexto;
    }

    public Integer getNuTipoGenerador() {
        return nuTipoGenerador;
    }

    public void setNuTipoGenerador(Integer nuTipoGenerador) {
        this.nuTipoGenerador = nuTipoGenerador;
    }

    public String getVcCodigoDependencia() {
        return vcCodigoDependencia;
    }

    public void setVcCodigoDependencia(String vcCodigoDependencia) {
        this.vcCodigoDependencia = vcCodigoDependencia;
    }

    public Integer getNuAviso() {
        return nuAviso;
    }

    public void setNuAviso(Integer nuAviso) {
        this.nuAviso = nuAviso;
    }

    public Integer getNuCantidadArchivos() {
        return nuCantidadArchivos;
    }

    public void setNuCantidadArchivos(Integer nuCantidadArchivos) {
        this.nuCantidadArchivos = nuCantidadArchivos;
    }

    public String getVcCodigoEtiqueta() {
        return vcCodigoEtiqueta;
    }

    public void setVcCodigoEtiqueta(String vcCodigoEtiqueta) {
        this.vcCodigoEtiqueta = vcCodigoEtiqueta;
    }

    public Integer getNuMensaje() {
        return nuMensaje;
    }

    public void setNuMensaje(Integer nuMensaje) {
        this.nuMensaje = nuMensaje;
    }

    public String getVcCodigoCarpeta() {
        return vcCodigoCarpeta;
    }

    public void setVcCodigoCarpeta(String vcCodigoCarpeta) {
        this.vcCodigoCarpeta = vcCodigoCarpeta;
    }

    public String getVcNumeroRuc() {
        return vcNumeroRuc;
    }

    public void setVcNumeroRuc(String vcNumeroRuc) {
        this.vcNumeroRuc = vcNumeroRuc;
    }

        // Método getter explícito
    public Long getNuCodigoMensaje() {
        return nuCodigoMensaje;
    }

    // Método setter explícito
    public void setNuCodigoMensaje(Long nuCodigoMensaje) {
        this.nuCodigoMensaje = nuCodigoMensaje;
    }
}