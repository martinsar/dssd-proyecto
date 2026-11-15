package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import ar.edu.unlp.dssd.exception.BonitaException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;

/** [Entrega final] Política de las tareas finales según rescuesync.bonita.integracion-completa */
class TareasFinalesBonitaServiceTest {

    private final BonitaService bonita = mock(BonitaService.class);

    @Test
    void sinIntegracionCompleta_unaTareaAusenteOUnFalloSoloSeRegistran() {
        TareasFinalesBonitaService servicio = new TareasFinalesBonitaService(bonita, false);
        when(bonita.seleccionarOfertas(100L)).thenReturn(false);
        when(bonita.finalizarActividades(100L)).thenThrow(new BonitaException("Bonita caído"));

        assertThatCode(() -> servicio.seleccionarOfertas(100L)).doesNotThrowAnyException();
        assertThatCode(() -> servicio.finalizarActividades(100L)).doesNotThrowAnyException();
    }

    @Test
    void sinIntegracionCompletaYSinCaso_noLlamaABonita() {
        TareasFinalesBonitaService servicio = new TareasFinalesBonitaService(bonita, false);

        assertThatCode(() -> servicio.finalizarActividades(null)).doesNotThrowAnyException();
        verifyNoInteractions(bonita);
    }

    @Test
    void conIntegracionCompleta_unaTareaAusenteResponde409() {
        TareasFinalesBonitaService servicio = new TareasFinalesBonitaService(bonita, true);
        when(bonita.seleccionarOfertas(100L)).thenReturn(false);

        assertThatThrownBy(() -> servicio.seleccionarOfertas(100L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("selección de ofertas");
    }

    @Test
    void conIntegracionCompleta_unFalloDeBonitaSePropaga() {
        TareasFinalesBonitaService servicio = new TareasFinalesBonitaService(bonita, true);
        when(bonita.finalizarActividades(100L)).thenThrow(new BonitaException("Bonita caído"));

        assertThatThrownBy(() -> servicio.finalizarActividades(100L)).isInstanceOf(BonitaException.class);
    }

    @Test
    void conIntegracionCompleta_unaTareaEjecutadaNoFalla() {
        TareasFinalesBonitaService servicio = new TareasFinalesBonitaService(bonita, true);
        when(bonita.finalizarActividades(100L)).thenReturn(true);

        assertThatCode(() -> servicio.finalizarActividades(100L)).doesNotThrowAnyException();
    }
}
