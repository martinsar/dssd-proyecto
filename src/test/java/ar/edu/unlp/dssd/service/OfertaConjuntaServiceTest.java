package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.OfertaConjuntaOngDto;
import ar.edu.unlp.dssd.dto.OfertaConjuntaRequest;
import ar.edu.unlp.dssd.dto.ParticipanteConjuntaDto;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.OfertaConjunta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.OfertaConjuntaRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

/** [Entrega final] Regla 2: alta de ofertas conjuntas sin invitaciones */
@ExtendWith(MockitoExtension.class)
class OfertaConjuntaServiceTest {

    @Mock private OfertaConjuntaRepository ofertaConjuntaRepository;
    @Mock private ConvocatoriaRepository convocatoriaRepository;
    @Mock private OrganizacionRepository organizacionRepository;
    @Mock private OfertaRepository ofertaRepository;
    @Mock private OfertaService ofertaService;
    @InjectMocks private OfertaConjuntaService servicio;

    private final SesionUsuario sesionCaritas = new SesionUsuario("t", 3L, "caritas", "Rosa", "Representante",
            Rol.ONG, 1L, "Cáritas", null, Instant.now());

    private static Organizacion organizacion(Long id, String nombre) {
        Organizacion o = new Organizacion();
        o.setId(id);
        o.setNombre(nombre);
        o.setCodigoOng("ONG-" + id);
        return o;
    }

    private static Convocatoria convocatoria(boolean abierta) {
        Convocatoria c = new Convocatoria();
        c.setId(7L);
        c.setEstado(EstadoConvocatoria.PUBLICADA);
        c.setFechaApertura(LocalDateTime.now().minusDays(1));
        c.setFechaCierre(abierta ? LocalDateTime.now().plusDays(1) : LocalDateTime.now().minusMinutes(1));
        return c;
    }

    @Test
    void crear_dejaALaDuenaPrimeroYFaltanTodasHastaQueCarguenLotes() {
        Organizacion caritas = organizacion(1L, "Cáritas");
        Organizacion cruzRoja = organizacion(2L, "Cruz Roja Argentina");
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria(true)));
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(caritas));
        when(organizacionRepository.findById(2L)).thenReturn(Optional.of(cruzRoja));
        when(ofertaConjuntaRepository.save(any(OfertaConjunta.class))).then(invocacion -> {
            OfertaConjunta c = invocacion.getArgument(0);
            c.setId(5L);
            return c;
        });
        when(ofertaRepository.findByOfertaConjuntaId(5L)).thenReturn(List.of());

        OfertaConjuntaOngDto dto = servicio.crear(new OfertaConjuntaRequest(7L, List.of(2L)), sesionCaritas);

        assertThat(dto.esDuena()).isTrue();
        assertThat(dto.ventanaAbierta()).isTrue();
        assertThat(dto.organizacionDuena().nombre()).isEqualTo("Cáritas");
        assertThat(dto.participantes()).extracting(ParticipanteConjuntaDto::nombre)
                .containsExactly("Cáritas", "Cruz Roja Argentina");
        assertThat(dto.valida()).isFalse();
        assertThat(dto.faltan()).containsExactly("Cáritas", "Cruz Roja Argentina");
    }

    @Test
    void crear_sinParticipantes_responde400() {
        assertThatThrownBy(() -> servicio.crear(new OfertaConjuntaRequest(7L, List.of()), sesionCaritas))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crear_incluyendoALaPropia_responde409() {
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria(true)));

        assertThatThrownBy(() -> servicio.crear(new OfertaConjuntaRequest(7L, List.of(2L, 1L)), sesionCaritas))
                .isInstanceOf(ReglaNegocioException.class);
        verify(ofertaConjuntaRepository, never()).save(any());
    }

    @Test
    void crear_repitiendoUnaOrganizacion_responde409() {
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria(true)));

        assertThatThrownBy(() -> servicio.crear(new OfertaConjuntaRequest(7L, List.of(2L, 2L)), sesionCaritas))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void crear_conLaVentanaCerrada_responde409() {
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria(false)));

        assertThatThrownBy(() -> servicio.crear(new OfertaConjuntaRequest(7L, List.of(2L)), sesionCaritas))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void obtener_unaConjuntaDondeNoParticipa_responde404() {
        OfertaConjunta ajena = new OfertaConjunta();
        ajena.setId(5L);
        ajena.setOrganizacionDuena(organizacion(2L, "Cruz Roja Argentina"));
        ajena.getParticipantes().add(organizacion(3L, "Bomberos"));
        when(ofertaConjuntaRepository.findById(5L)).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> servicio.obtener(5L, sesionCaritas)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
