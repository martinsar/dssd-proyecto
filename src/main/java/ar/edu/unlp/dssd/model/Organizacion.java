package ar.edu.unlp.dssd.model;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "organizacion", schema = "dssd")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Organizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Entrega final] Nombre de la organización
    @Column(name = "nombre", length = 150)
    private String nombre;

    // [Entrega final] Código con el que el Sistema Nacional identifica a la ONG
    @Column(name = "codigo_ong", length = 100, unique = true)
    private String codigoOng;

    private String tipo;

    @Column(name = "objeto_social")
    private String objetoSocial;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrada_por")
    private Usuario registradaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validado_por")
    private Usuario validadoPor;

    @JsonIgnore
    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Oferta> ofertas;

    // [Entrega final] Tipos de recurso en los que se especializa la ONG
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "organizacion_especialidad", schema = "dssd",
            joinColumns = @JoinColumn(name = "organizacion_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_recurso_id"))
    private Set<TipoRecurso> especialidades = new LinkedHashSet<>();


    public Set<TipoRecurso> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(Set<TipoRecurso> especialidades) {
        this.especialidades = especialidades;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoOng() {
        return codigoOng;
    }

    public void setCodigoOng(String codigoOng) {
        this.codigoOng = codigoOng;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getObjetoSocial() {
        return objetoSocial;
    }

    public void setObjetoSocial(String objetoSocial) {
        this.objetoSocial = objetoSocial;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getRegistradaPor() {
        return registradaPor;
    }

    public void setRegistradaPor(Usuario registradaPor) {
        this.registradaPor = registradaPor;
    }

    public Usuario getValidadoPor() {
        return validadoPor;
    }

    public void setValidadoPor(Usuario validadoPor) {
        this.validadoPor = validadoPor;
    }

    public List<Oferta> getOfertas() {
        return ofertas;
    }

    public void setOfertas(List<Oferta> ofertas) {
        this.ofertas = ofertas;
    }
}