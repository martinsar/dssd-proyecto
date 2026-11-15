package ar.edu.unlp.dssd.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** [Entrega final] Alta o actualización de un recurso del inventario */
public record InventarioRequest(
        @NotNull(message = "La cantidad disponible es obligatoria")
        @PositiveOrZero(message = "La cantidad disponible no puede ser negativa")
        Integer cantidadDisponible,

        @Size(max = 255, message = "La descripción puede tener hasta 255 caracteres")
        String descripcion) {
}
