package ar.edu.unlp.dssd.model;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "usuario", schema = "dssd")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    
    @Column(unique = true, nullable = false, length = 11)
    private String cuil;
    
    private String rol;
    
    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @JsonIgnore
    @OneToMany(mappedBy = "creadoPor", cascade = CascadeType.ALL)
    private List<Emergencia> emergenciasCreadas;

    @JsonIgnore
    @OneToMany(mappedBy = "registradaPor", cascade = CascadeType.ALL)
    private List<Organizacion> organizacionesRegistradas;

    @JsonIgnore
    @OneToMany(mappedBy = "validadoPor", cascade = CascadeType.ALL)
    private List<Organizacion> organizacionesValidadas;

    
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCuil() {
        return cuil;
    }

    public void setCuil(String cuil) {
        this.cuil = cuil;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<Emergencia> getEmergenciasCreadas() {
        return emergenciasCreadas;
    }

    public void setEmergenciasCreadas(List<Emergencia> emergenciasCreadas) {
        this.emergenciasCreadas = emergenciasCreadas;
    }

    public List<Organizacion> getOrganizacionesRegistradas() {
        return organizacionesRegistradas;
    }

    public void setOrganizacionesRegistradas(List<Organizacion> organizacionesRegistradas) {
        this.organizacionesRegistradas = organizacionesRegistradas;
    }

    public List<Organizacion> getOrganizacionesValidadas() {
        return organizacionesValidadas;
    }

    public void setOrganizacionesValidadas(List<Organizacion> organizacionesValidadas) {
        this.organizacionesValidadas = organizacionesValidadas;
    }
}