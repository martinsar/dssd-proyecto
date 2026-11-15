package ar.edu.unlp.dssd.dto;

import ar.edu.unlp.dssd.model.TipoRecurso;

/** [Entrega final] Tipo de recurso en el que se especializa la ONG */
public record EspecialidadDto(Long tipoRecursoId, String codigo, String nombre) {

    public static EspecialidadDto desde(TipoRecurso t) {
        return new EspecialidadDto(t.getId(), t.getCodigo(), t.getNombre());
    }
}
