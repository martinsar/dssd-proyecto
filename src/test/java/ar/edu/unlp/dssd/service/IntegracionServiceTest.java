package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.OfertasConsolidadasDto;
import ar.edu.unlp.dssd.dto.ResultadoValidacionRequest;
import ar.edu.unlp.dssd.dto.ValidacionesRequest;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;

/** [Entrega final] Tests del consolidado de ofertas y del registro de validaciones para Bonita. */
@ExtendWith(MockitoExtension.class)
class IntegracionServiceTest {

    @Mock
    private EmergenciaRepository emergenciaRepository;

    @Mock
    private ConvocatoriaRepository convocatoriaRepository;

    @Mock
    private NecesidadRepository necesidadRepository;

    @Mock
    private OfertaRepository ofertaRepository;

    @InjectMocks
    private IntegracionService integracionService;

    private static final Long ID_EMERGENCIA = 5L;
    private static final Long ID_CONVOCATORIA = 9L;
    private static final LocalDateTime CIERRE = LocalDateTime.of(2030, 1, 15, 18, 0);

    private final TipoRecurso agua = tipo(1L, "AGUA", "Agua potable");
    private final TipoRecurso frazadas = tipo(2L, "FRAZADAS", "Frazadas");
    private final Organizacion caritas = organizacion(1L, "ONG-CARITAS", "Cáritas");
    private final Organizacion cruzRoja = organizacion(2L, "ONG-CRUZ-ROJA", "Cruz Roja Argentina");

    private static TipoRecurso tipo(Long id, String codigo, String nombre) {
        TipoRecurso t = new TipoRecurso();
        t.setId(id);
        t.setCodigo(codigo);
        t.setNombre(nombre);
        return t;
    }

    private static Organizacion organizacion(Long id, String codigo, String nombre) {
        Organizacion o = new Organizacion();
        o.setId(id);
        o.setCodigoOng(codigo);
        o.setNombre(nombre);
        return o;
    }

    private Convocatoria convocatoria() {
        Emergencia emergencia = new Emergencia();
        emergencia.setId(ID_EMERGENCIA);
        Convocatoria c = new Convocatoria();
        c.setId(ID_CONVOCATORIA);
        c.setEmergencia(emergencia);
        c.setFechaCierre(CIERRE);
        return c;
    }

    private static Necesidad necesidad(Long id, TipoRecurso tipo, int cantidad) {
        Necesidad n = new Necesidad();
        n.setId(id);
        n.setTipoRecurso(tipo);
        n.setCantidad(cantidad);
        return n;
    }

    private static Oferta oferta(Long id, Convocatoria convocatoria, Organizacion organizacion, TipoRecurso tipo,
                                 int cantidad, EstadoOferta estado) {
        Oferta o = new Oferta();
        o.setId(id);
        o.setConvocatoria(convocatoria);
        o.setOrganizacion(organizacion);
        o.setTipoRecurso(tipo);
        o.setCantidad(cantidad);
        o.setDescripcion("Oferta " + id);
        o.setEstado(estado);
        return o;
    }

    private void conConvocatoria(Convocatoria convocatoria, List<Necesidad> necesidades, List<Oferta> ofertas) {
        when(emergenciaRepository.existsById(ID_EMERGENCIA)).thenReturn(true);
        when(convocatoriaRepository.findByEmergenciaIdOrderByIdDesc(ID_EMERGENCIA)).thenReturn(List.of(convocatoria));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(necesidades);
        when(ofertaRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(ofertas);
    }

    // ------------------------------------------------------------------
    // Consolidado
    // ------------------------------------------------------------------

    @Test
    void consolidado_conCoberturaCompleta_devuelveLotesYOfertasConCodigos() {
        Convocatoria c = convocatoria();
        conConvocatoria(c,
                List.of(necesidad(3L, agua, 500), necesidad(4L, frazadas, 100)),
                List.of(oferta(21L, c, caritas, agua, 300, EstadoOferta.PENDIENTE),
                        oferta(22L, c, cruzRoja, agua, 300, EstadoOferta.VALIDADA),
                        oferta(23L, c, caritas, frazadas, 100, EstadoOferta.PENDIENTE)));

        OfertasConsolidadasDto dto = integracionService.obtenerConsolidado(ID_EMERGENCIA, null);

        assertThat(dto.emergenciaId()).isEqualTo(ID_EMERGENCIA);
        assertThat(dto.convocatoriaId()).isEqualTo(ID_CONVOCATORIA);
        assertThat(dto.fechaCierre()).isEqualTo(CIERRE);
        assertThat(dto.coberturaCompleta()).isTrue();
        assertThat(dto.lotes()).hasSize(2);
        assertThat(dto.lotes().get(0).codigoTipoRecurso()).isEqualTo("AGUA");
        assertThat(dto.lotes().get(0).cantidadRequerida()).isEqualTo(500);
        assertThat(dto.lotes().get(0).cantidadOfertada()).isEqualTo(600);
        assertThat(dto.ofertas()).hasSize(3);
        assertThat(dto.ofertas().get(1).codigoOng()).isEqualTo("ONG-CRUZ-ROJA");
        assertThat(dto.ofertas().get(1).organizacion()).isEqualTo("Cruz Roja Argentina");
        assertThat(dto.ofertas().get(1).codigoTipoRecurso()).isEqualTo("AGUA");
        assertThat(dto.ofertas().get(1).estado()).isEqualTo(EstadoOferta.VALIDADA);
    }

    @Test
    void consolidado_conCoberturaParcial_loInforma() {
        Convocatoria c = convocatoria();
        conConvocatoria(c,
                List.of(necesidad(3L, agua, 500), necesidad(4L, frazadas, 100)),
                List.of(oferta(21L, c, caritas, agua, 600, EstadoOferta.PENDIENTE)));

        OfertasConsolidadasDto dto = integracionService.obtenerConsolidado(ID_EMERGENCIA, null);

        assertThat(dto.coberturaCompleta()).isFalse();
        assertThat(dto.lotes().get(1).cantidadOfertada()).isZero();
    }

    @Test
    void consolidado_elFiltroDeEstadoSoloAfectaAlListado() {
        Convocatoria c = convocatoria();
        conConvocatoria(c,
                List.of(necesidad(3L, agua, 500)),
                List.of(oferta(21L, c, caritas, agua, 300, EstadoOferta.PENDIENTE),
                        oferta(22L, c, cruzRoja, agua, 300, EstadoOferta.VALIDADA)));

        OfertasConsolidadasDto dto = integracionService.obtenerConsolidado(ID_EMERGENCIA, EstadoOferta.VALIDADA);

        assertThat(dto.ofertas()).extracting(o -> o.ofertaId()).containsExactly(22L);
        assertThat(dto.lotes().get(0).cantidadOfertada()).isEqualTo(600);
        assertThat(dto.coberturaCompleta()).isTrue();
    }

    @Test
    void consolidado_usaLaConvocatoriaMasReciente() {
        Convocatoria reciente = convocatoria();
        Convocatoria vieja = convocatoria();
        vieja.setId(1L);
        when(emergenciaRepository.existsById(ID_EMERGENCIA)).thenReturn(true);
        when(convocatoriaRepository.findByEmergenciaIdOrderByIdDesc(ID_EMERGENCIA))
                .thenReturn(List.of(reciente, vieja));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of());
        when(ofertaRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of());

        assertThat(integracionService.obtenerConsolidado(ID_EMERGENCIA, null).convocatoriaId())
                .isEqualTo(ID_CONVOCATORIA);
    }

    @Test
    void consolidado_sinConvocatoria_lanzaRecursoNoEncontrado() {
        when(emergenciaRepository.existsById(ID_EMERGENCIA)).thenReturn(true);
        when(convocatoriaRepository.findByEmergenciaIdOrderByIdDesc(ID_EMERGENCIA)).thenReturn(List.of());

        assertThatThrownBy(() -> integracionService.obtenerConsolidado(ID_EMERGENCIA, null))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void consolidado_conEmergenciaInexistente_lanzaRecursoNoEncontrado() {
        when(emergenciaRepository.existsById(ID_EMERGENCIA)).thenReturn(false);

        assertThatThrownBy(() -> integracionService.obtenerConsolidado(ID_EMERGENCIA, null))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------
    // Validaciones
    // ------------------------------------------------------------------

    @Test
    void validaciones_marcanValidadaONoHabilitadaYLaFecha() {
        Convocatoria c = convocatoria();
        Oferta habilitada = oferta(21L, c, caritas, agua, 300, EstadoOferta.PENDIENTE);
        Oferta rechazada = oferta(22L, c, cruzRoja, agua, 300, EstadoOferta.PENDIENTE);
        when(emergenciaRepository.existsById(ID_EMERGENCIA)).thenReturn(true);
        when(ofertaRepository.findById(21L)).thenReturn(Optional.of(habilitada));
        when(ofertaRepository.findById(22L)).thenReturn(Optional.of(rechazada));

        int registradas = integracionService.registrarValidaciones(ID_EMERGENCIA, new ValidacionesRequest(List.of(
                new ResultadoValidacionRequest(21L, true),
                new ResultadoValidacionRequest(22L, false))));

        assertThat(registradas).isEqualTo(2);
        assertThat(habilitada.getEstado()).isEqualTo(EstadoOferta.VALIDADA);
        assertThat(rechazada.getEstado()).isEqualTo(EstadoOferta.NO_HABILITADA);
        assertThat(habilitada.getFechaValidacion()).isEqualTo(LocalDate.now());
        assertThat(rechazada.getFechaValidacion()).isEqualTo(LocalDate.now());
        verify(ofertaRepository).saveAll(List.of(habilitada, rechazada));
    }

    @Test
    void validaciones_conOfertaDeOtraEmergencia_lanzaRecursoNoEncontradoSinGuardar() {
        Convocatoria otra = convocatoria();
        otra.getEmergencia().setId(99L);
        when(emergenciaRepository.existsById(ID_EMERGENCIA)).thenReturn(true);
        when(ofertaRepository.findById(21L))
                .thenReturn(Optional.of(oferta(21L, otra, caritas, agua, 300, EstadoOferta.PENDIENTE)));

        assertThatThrownBy(() -> integracionService.registrarValidaciones(ID_EMERGENCIA,
                new ValidacionesRequest(List.of(new ResultadoValidacionRequest(21L, true)))))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(ofertaRepository, never()).saveAll(any());
    }

    @Test
    void validaciones_conOfertaInexistente_lanzaRecursoNoEncontrado() {
        when(emergenciaRepository.existsById(ID_EMERGENCIA)).thenReturn(true);
        when(ofertaRepository.findById(21L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> integracionService.registrarValidaciones(ID_EMERGENCIA,
                new ValidacionesRequest(List.of(new ResultadoValidacionRequest(21L, true)))))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void validaciones_sinResultados_lanzaIllegalArgument() {
        assertThatThrownBy(() -> integracionService.registrarValidaciones(ID_EMERGENCIA,
                new ValidacionesRequest(List.of())))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> integracionService.registrarValidaciones(ID_EMERGENCIA, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validaciones_conResultadoIncompleto_lanzaIllegalArgument() {
        when(emergenciaRepository.existsById(ID_EMERGENCIA)).thenReturn(true);

        assertThatThrownBy(() -> integracionService.registrarValidaciones(ID_EMERGENCIA,
                new ValidacionesRequest(List.of(new ResultadoValidacionRequest(21L, null)))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(ofertaRepository, never()).saveAll(any());
    }
}
