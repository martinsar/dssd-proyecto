package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.Notificacion;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.TipoNotificacion;
import ar.edu.unlp.dssd.repository.NotificacionRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;

/** [Entrega final] Regla 11: notificaciones a las ONGs */
@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private OrganizacionRepository organizacionRepository;
    @InjectMocks private NotificacionService notificacionService;

    private static Organizacion organizacion(Long id) {
        Organizacion o = new Organizacion();
        o.setId(id);
        return o;
    }

    private static Convocatoria convocatoria() {
        Emergencia e = new Emergencia();
        e.setId(12L);
        e.setTipoDesastre("Inundación");
        e.setZonaAfectada("Barrio San Carlos");
        Convocatoria c = new Convocatoria();
        c.setId(7L);
        c.setEmergencia(e);
        c.setFechaCierre(LocalDateTime.of(2030, 11, 20, 18, 0));
        return c;
    }

    @Test
    void notificarConvocatoria_avisaATodasLasOrganizaciones() {
        when(organizacionRepository.findAll()).thenReturn(List.of(organizacion(1L), organizacion(2L)));
        when(notificacionRepository.save(any(Notificacion.class))).then(returnsFirstArg());

        int enviadas = notificacionService.notificarConvocatoria(convocatoria(), TipoNotificacion.NUEVA_CONVOCATORIA);

        assertThat(enviadas).isEqualTo(2);
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(2)).save(captor.capture());
        Notificacion primera = captor.getAllValues().get(0);
        assertThat(primera.getTipo()).isEqualTo(TipoNotificacion.NUEVA_CONVOCATORIA);
        assertThat(primera.getTitulo()).isEqualTo("Nueva convocatoria: Inundación en Barrio San Carlos");
        assertThat(primera.getMensaje()).contains("20/11/2030 18:00");
        assertThat(primera.isLeida()).isFalse();
    }

    @Test
    void notificarResultado_noDuplicaLaNotificacionDeUnLote() {
        Oferta oferta = new Oferta();
        oferta.setId(21L);
        oferta.setOrganizacion(organizacion(1L));
        oferta.setConvocatoria(convocatoria());
        when(notificacionRepository.existsByOfertaIdAndOrganizacionIdAndTipo(21L, 1L, TipoNotificacion.OFERTA_ADJUDICADA))
                .thenReturn(true);

        assertThat(notificacionService.notificarResultado(oferta, TipoNotificacion.OFERTA_ADJUDICADA)).isFalse();
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void marcarLeida_deOtraOrganizacion_responde404() {
        when(notificacionRepository.findByIdAndOrganizacionId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificacionService.marcarLeida(1L, 5L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
