package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.NecesidadRequest;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.TipoRecursoRepository;

@ExtendWith(MockitoExtension.class)
class NecesidadServiceTest {

    @Mock
    private NecesidadRepository necesidadRepository;

    @Mock
    private ConvocatoriaRepository convocatoriaRepository;

    @Mock
    private TipoRecursoRepository tipoRecursoRepository;

    @InjectMocks
    private NecesidadService necesidadService;

    private static final Long ID_CONVOCATORIA = 1L;
    private static final Long ID_TIPO_RECURSO = 3L;

    private Convocatoria convocatoria(EstadoConvocatoria estado) {
        Convocatoria c = new Convocatoria();
        c.setId(ID_CONVOCATORIA);
        c.setEstado(estado);
        return c;
    }

    // ------------------------------------------------------------------
    // agregar
    // ------------------------------------------------------------------

    @Test
    void agregar_siLaConvocatoriaNoExiste_lanzaRecursoNoEncontrado() {
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> necesidadService.agregar(ID_CONVOCATORIA,
                new NecesidadRequest(ID_TIPO_RECURSO, 5, "Frazadas")))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(necesidadRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoConvocatoria.class, names = {"PUBLICADA", "CERRADA"})
    void agregar_siLaConvocatoriaNoEstaEnBorrador_lanzaReglaNegocio(EstadoConvocatoria estado) {
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(convocatoria(estado)));

        assertThatThrownBy(() -> necesidadService.agregar(ID_CONVOCATORIA,
                new NecesidadRequest(ID_TIPO_RECURSO, 5, "Frazadas")))
                .isInstanceOf(ReglaNegocioException.class);
        verify(necesidadRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void agregar_conCantidadMenorA1_lanzaIllegalArgumentException(int cantidad) {
        when(convocatoriaRepository.findById(ID_CONVOCATORIA))
                .thenReturn(Optional.of(convocatoria(EstadoConvocatoria.BORRADOR)));

        assertThatThrownBy(() -> necesidadService.agregar(ID_CONVOCATORIA,
                new NecesidadRequest(ID_TIPO_RECURSO, cantidad, "Frazadas")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(necesidadRepository, never()).save(any());
    }

    @Test
    void agregar_conCantidadNula_lanzaIllegalArgumentException() {
        when(convocatoriaRepository.findById(ID_CONVOCATORIA))
                .thenReturn(Optional.of(convocatoria(EstadoConvocatoria.BORRADOR)));

        assertThatThrownBy(() -> necesidadService.agregar(ID_CONVOCATORIA,
                new NecesidadRequest(ID_TIPO_RECURSO, null, "Frazadas")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(necesidadRepository, never()).save(any());
    }

    @Test
    void agregar_sinTipoDeRecurso_lanzaIllegalArgumentException() {
        when(convocatoriaRepository.findById(ID_CONVOCATORIA))
                .thenReturn(Optional.of(convocatoria(EstadoConvocatoria.BORRADOR)));

        assertThatThrownBy(() -> necesidadService.agregar(ID_CONVOCATORIA,
                new NecesidadRequest(null, 5, "Frazadas")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(necesidadRepository, never()).save(any());
    }

    @Test
    void agregar_siElTipoDeRecursoNoExiste_lanzaRecursoNoEncontrado() {
        when(convocatoriaRepository.findById(ID_CONVOCATORIA))
                .thenReturn(Optional.of(convocatoria(EstadoConvocatoria.BORRADOR)));
        when(tipoRecursoRepository.findById(ID_TIPO_RECURSO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> necesidadService.agregar(ID_CONVOCATORIA,
                new NecesidadRequest(ID_TIPO_RECURSO, 5, "Frazadas")))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(necesidadRepository, never()).save(any());
    }

    @Test
    void agregar_casoFeliz_conCantidadMinima1_guardaLaNecesidadConTodosSusDatos() {
        Convocatoria convocatoria = convocatoria(EstadoConvocatoria.BORRADOR);
        TipoRecurso tipoRecurso = new TipoRecurso();
        tipoRecurso.setId(ID_TIPO_RECURSO);
        tipoRecurso.setNombre("Frazadas");
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(convocatoria));
        when(tipoRecursoRepository.findById(ID_TIPO_RECURSO)).thenReturn(Optional.of(tipoRecurso));
        when(necesidadRepository.save(any(Necesidad.class))).then(returnsFirstArg());

        Necesidad resultado = necesidadService.agregar(ID_CONVOCATORIA,
                new NecesidadRequest(ID_TIPO_RECURSO, 1, "Para 30 familias"));

        assertThat(resultado.getConvocatoria()).isSameAs(convocatoria);
        assertThat(resultado.getTipoRecurso()).isSameAs(tipoRecurso);
        assertThat(resultado.getCantidad()).isEqualTo(1);
        assertThat(resultado.getDescripcion()).isEqualTo("Para 30 familias");
        assertThat(resultado.getFechaCreacion()).isEqualTo(LocalDate.now());
        verify(necesidadRepository).save(resultado);
    }

    // ------------------------------------------------------------------
    // quitar
    // ------------------------------------------------------------------

    @Test
    void quitar_siLaConvocatoriaNoExiste_lanzaRecursoNoEncontrado() {
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> necesidadService.quitar(ID_CONVOCATORIA, 9L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(necesidadRepository, never()).delete(any());
    }

    @Test
    void quitar_siLaNecesidadEsDeOtraConvocatoria_lanzaRecursoNoEncontrado() {
        Convocatoria otra = new Convocatoria();
        otra.setId(2L);
        Necesidad necesidad = new Necesidad();
        necesidad.setId(9L);
        necesidad.setConvocatoria(otra);
        when(convocatoriaRepository.findById(ID_CONVOCATORIA))
                .thenReturn(Optional.of(convocatoria(EstadoConvocatoria.BORRADOR)));
        when(necesidadRepository.findById(9L)).thenReturn(Optional.of(necesidad));

        assertThatThrownBy(() -> necesidadService.quitar(ID_CONVOCATORIA, 9L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(necesidadRepository, never()).delete(any());
    }

    @Test
    void quitar_siLaConvocatoriaNoEstaEnBorrador_lanzaReglaNegocioYNoBorra() {
        Convocatoria publicada = convocatoria(EstadoConvocatoria.PUBLICADA);
        Necesidad necesidad = new Necesidad();
        necesidad.setId(9L);
        necesidad.setConvocatoria(publicada);
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(publicada));
        when(necesidadRepository.findById(9L)).thenReturn(Optional.of(necesidad));

        assertThatThrownBy(() -> necesidadService.quitar(ID_CONVOCATORIA, 9L))
                .isInstanceOf(ReglaNegocioException.class);
        verify(necesidadRepository, never()).delete(any());
    }

    @Test
    void quitar_casoFeliz_borraLaNecesidad() {
        Convocatoria borrador = convocatoria(EstadoConvocatoria.BORRADOR);
        Necesidad necesidad = new Necesidad();
        necesidad.setId(9L);
        necesidad.setConvocatoria(borrador);
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(borrador));
        when(necesidadRepository.findById(9L)).thenReturn(Optional.of(necesidad));

        necesidadService.quitar(ID_CONVOCATORIA, 9L);

        verify(necesidadRepository).delete(necesidad);
    }
}
