package ar.edu.unlp.dssd.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** [Entrega final] Foto de un lote después de cada cambio (trazabilidad) */
@Entity
@Table(name = "oferta_version", schema = "dssd",
        uniqueConstraints = @UniqueConstraint(name = "uk_oferta_version_numero",
                columnNames = {"oferta_id", "numero_version"}))
public class OfertaVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Oferta oferta;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    // Null si el cambio lo hizo el sistema o Bonita
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "motivo", length = 200)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_recurso_id")
    private TipoRecurso tipoRecurso;

    private Integer cantidad;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 40, columnDefinition = "varchar(40)")
    private EstadoOferta estado;

    /** Se toma la foto del lote en su versión actual */
    public static OfertaVersion desde(Oferta oferta, Usuario usuario, String motivo) {
        OfertaVersion version = new OfertaVersion();
        version.setOferta(oferta);
        version.setNumeroVersion(oferta.getNumeroVersion() == null ? 1 : oferta.getNumeroVersion());
        version.setFechaHora(LocalDateTime.now());
        version.setUsuario(usuario);
        version.setMotivo(motivo);
        version.setTipoRecurso(oferta.getTipoRecurso());
        version.setCantidad(oferta.getCantidad());
        version.setDescripcion(oferta.getDescripcion());
        version.setEstado(oferta.getEstado());
        return version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Oferta getOferta() {
        return oferta;
    }

    public void setOferta(Oferta oferta) {
        this.oferta = oferta;
    }

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public TipoRecurso getTipoRecurso() {
        return tipoRecurso;
    }

    public void setTipoRecurso(TipoRecurso tipoRecurso) {
        this.tipoRecurso = tipoRecurso;
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
}
