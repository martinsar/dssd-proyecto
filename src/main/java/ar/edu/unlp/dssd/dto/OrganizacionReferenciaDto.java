package ar.edu.unlp.dssd.dto;

import ar.edu.unlp.dssd.model.Organizacion;

/** [Entrega final] Id y nombre de una organización */
public record OrganizacionReferenciaDto(Long id, String nombre) {

    public static OrganizacionReferenciaDto desde(Organizacion o) {
        return o == null ? null : new OrganizacionReferenciaDto(o.getId(), o.getNombre());
    }
}
