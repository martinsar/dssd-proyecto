package ar.edu.unlp.dssd.dto;

import ar.edu.unlp.dssd.model.Organizacion;

/** [Entrega final] Organización que se puede sumar a una oferta conjunta */
public record OrganizacionResumenDto(Long id, String codigoOng, String nombre) {

    public static OrganizacionResumenDto desde(Organizacion o) {
        return new OrganizacionResumenDto(o.getId(), o.getCodigoOng(), o.getNombre());
    }
}
