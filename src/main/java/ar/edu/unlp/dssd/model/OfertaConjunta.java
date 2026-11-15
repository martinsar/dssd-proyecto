package ar.edu.unlp.dssd.model;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * [Entrega final] Oferta conjunta: la ONG dueña la crea con sus participantes y cada una carga sus lotes.
 * Los participantes no se modifican después de creada.
 */
@Entity
@Table(name = "oferta_conjunta", schema = "dssd")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OfertaConjunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "convocatoria_id", nullable = false)
    private Convocatoria convocatoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_duena_id", nullable = false)
    private Organizacion organizacionDuena;

    // [Entrega final] Participantes sin la dueña; la tabla de unión no se modifica después del alta
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "oferta_conjunta_participante", schema = "dssd",
            joinColumns = @JoinColumn(name = "oferta_conjunta_id"),
            inverseJoinColumns = @JoinColumn(name = "organizacion_id"))
    private Set<Organizacion> participantes = new LinkedHashSet<>();

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Convocatoria getConvocatoria() {
        return convocatoria;
    }

    public void setConvocatoria(Convocatoria convocatoria) {
        this.convocatoria = convocatoria;
    }

    public Organizacion getOrganizacionDuena() {
        return organizacionDuena;
    }

    public void setOrganizacionDuena(Organizacion organizacionDuena) {
        this.organizacionDuena = organizacionDuena;
    }

    public Set<Organizacion> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(Set<Organizacion> participantes) {
        this.participantes = participantes;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
