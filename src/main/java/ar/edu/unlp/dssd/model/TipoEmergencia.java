package ar.edu.unlp.dssd.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_emergencia", schema = "dssd")
public class TipoEmergencia {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
	@Column(name = "tipo_desastre")
    private String tipoDesastre;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTipoDesastre() {
		return tipoDesastre;
	}

	public void setTipoDesastre(String tipoDesastre) {
		this.tipoDesastre = tipoDesastre;
	} 


}
