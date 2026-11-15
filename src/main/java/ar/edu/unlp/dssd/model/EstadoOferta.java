package ar.edu.unlp.dssd.model;

/**
 * [Entrega final] Estados de un lote: PENDIENTE, después VALIDADA o NO_HABILITADA (Sistema Nacional),
 * SELECCIONADA o NO_SELECCIONADA (municipio), ADJUDICADA (notificación formal) y FINALIZADA (la ONG).
 * RETIRADA: la ONG lo retiró con la ventana abierta.
 */
public enum EstadoOferta {
    PENDIENTE,
    VALIDADA,
    NO_HABILITADA,
    SELECCIONADA,
    NO_SELECCIONADA,
    ADJUDICADA,
    FINALIZADA,
    RETIRADA
}
