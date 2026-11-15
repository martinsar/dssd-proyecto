package ar.edu.unlp.dssd.model;

import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tipo_recurso", schema = "dssd")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TipoRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Entrega final] Código estable del catálogo compartido con el Sistema Nacional
    @Column(name = "codigo", length = 50)
    private String codigo;

    private String nombre;

    @JsonIgnore
    @OneToMany(mappedBy = "tipoRecurso", cascade = CascadeType.ALL)
    private List<Necesidad> necesidades;

    @JsonIgnore
    @OneToMany(mappedBy = "tipoRecurso", cascade = CascadeType.ALL)
    private List<Oferta> ofertas;

    // --- GETTERS Y SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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