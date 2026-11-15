package ar.edu.unlp.dssd.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "oferta", schema = "dssd")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer cantidad;
    private String descripcion;
    // [Entrega final] Se guarda como texto en una columna varchar (sin CHECK)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "varchar(255)")
    private EstadoOferta estado;
    private String tipo;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(name = "fecha_validacion")
    private LocalDate fechaValidacion;

    @Column(name = "nivel_aprobacion")
    private String nivelAprobacion;

    @Column(name = "perfil_competencia")
    private String perfilCompetencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_id")
    private Organizacion organizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_recurso_id")
    private TipoRecurso tipoRecurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convocatoria_id")
    private Convocatoria convocatoria;

    // [Entrega final] Oferta conjunta a la que pertenece el lote (null si es individual)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_conjunta_id")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.SET_NULL)
    private OfertaConjunta ofertaConjunta;

    // [Entrega final] Versión vigente del lote (cada cambio la incrementa)
    @Column(name = "numero_version")
    private Integer numeroVersion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "fecha_adjudicacion")
    private LocalDateTime fechaAdjudicacion;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Column(name = "informe_finalizacion", length = 500)
    private String informeFinalizacion;


    /** [Entrega final] Se pasa a la versión siguiente y se registra la fecha del cambio */
    public void avanzarVersion() {
        this.numeroVersion = (numeroVersion == null ? 1 : numeroVersion) + 1;
        this.fechaModificacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoOferta getEstado() {
        return estado;
    }

    public void setEstado(EstadoOferta estado) {
        this.estado = estado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDate getFechaValidacion() {
        return fechaValidacion;
    }

    public void setFechaValidacion(LocalDate fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }

    public String getNivelAprobacion() {
        return nivelAprobacion;
    }

    public void setNivelAprobacion(String nivelAprobacion) {
        this.nivelAprobacion = nivelAprobacion;
    }

    public String getPerfilCompetencia() {
        return perfilCompetencia;
    }

    public void setPerfilCompetencia(String perfilCompetencia) {
        this.perfilCompetencia = perfilCompetencia;
    }

    public Organizacion getOrganizacion() {
        return organizacion;
    }

    public void setOrganizacion(Organizacion organizacion) {
        this.organizacion = organizacion;
    }

    public TipoRecurso getTipoRecurso() {
        return tipoRecurso;
    }

    public void setTipoRecurso(TipoRecurso tipoRecurso) {
        this.tipoRecurso = tipoRecurso;
    }

    public Convocatoria getConvocatoria() {
        return convocatoria;
    }

    public void setConvocatoria(Convocatoria convocatoria) {
        this.convocatoria = convocatoria;
    }

    public OfertaConjunta getOfertaConjunta() {
        return ofertaConjunta;
    }

    public void setOfertaConjunta(OfertaConjunta ofertaConjunta) {
        this.ofertaConjunta = ofertaConjunta;
    }

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public LocalDateTime getFechaAdjudicacion() {
        return fechaAdjudicacion;
    }

    public void setFechaAdjudicacion(LocalDateTime fechaAdjudicacion) {
        this.fechaAdjudicacion = fechaAdjudicacion;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public String getInformeFinalizacion() {
        return informeFinalizacion;
    }

    public void setInformeFinalizacion(String informeFinalizacion) {
        this.informeFinalizacion = informeFinalizacion;
    }
}