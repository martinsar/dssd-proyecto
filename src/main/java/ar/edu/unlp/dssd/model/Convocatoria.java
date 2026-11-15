package ar.edu.unlp.dssd.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "convocatoria", schema = "dssd")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Convocatoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se guarda como texto ("BORRADOR", "PUBLICADA", ...) para que la columna varchar existente siga sirviendo
    @Enumerated(EnumType.STRING)
    private EstadoConvocatoria estado;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    // Apertura y cierre llevan hora (el cierre se manda a Bonita como fecha y hora)
    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id")
    private Emergencia emergencia;

    @JsonIgnore
    @OneToMany(mappedBy = "convocatoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Necesidad> necesidades;

    @JsonIgnore
    @OneToMany(mappedBy = "convocatoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Oferta> ofertas;

    // [Entrega final] Se borran junto con la convocatoria
    @JsonIgnore
    @OneToMany(mappedBy = "convocatoria", cascade = CascadeType.REMOVE)
    private List<OfertaConjunta> ofertasConjuntas;

    // [Entrega final] Ronda de recepción de ofertas (sube con cada reapertura)
    @Column(name = "ronda")
    private Integer ronda;

    // [Entrega final] El coordinador aceptó seguir con cobertura parcial
    @Column(name = "cobertura_parcial_aceptada")
    private Boolean coberturaParcialAceptada;


    public Integer getRonda() {
        return ronda;
    }

    public void setRonda(Integer ronda) {
        this.ronda = ronda;
    }

    public Boolean getCoberturaParcialAceptada() {
        return coberturaParcialAceptada;
    }

    public void setCoberturaParcialAceptada(Boolean coberturaParcialAceptada) {
        this.coberturaParcialAceptada = coberturaParcialAceptada;
    }

    public List<OfertaConjunta> getOfertasConjuntas() {
        return ofertasConjuntas;
    }

    public void setOfertasConjuntas(List<OfertaConjunta> ofertasConjuntas) {
        this.ofertasConjuntas = ofertasConjuntas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoConvocatoria getEstado() {
        return estado;
    }

    public void setEstado(EstadoConvocatoria estado) {
        this.estado = estado;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Emergencia getEmergencia() {
        return emergencia;
    }

    public void setEmergencia(Emergencia emergencia) {
        this.emergencia = emergencia;
    }

    public List<Necesidad> getNecesidades() {
        return necesidades;
    }

    public void setNecesidades(List<Necesidad> necesidades) {
        this.necesidades = necesidades;
    }

    public List<Oferta> getOfertas() {
        return ofertas;
    }

    public void setOfertas(List<Oferta> ofertas) {
        this.ofertas = ofertas;
    }
}