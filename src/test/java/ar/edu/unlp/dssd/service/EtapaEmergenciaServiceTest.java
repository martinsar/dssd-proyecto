package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import ar.edu.unlp.dssd.dto.LoteDetalleDto;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.TipoRecurso;

/** [Entrega final] Una prueba por cada rama del cálculo de la etapa (regla 10) */
class EtapaEmergenciaServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2030, 1, 10, 12, 0);

    private final EtapaEmergenciaService servicio = new EtapaEmergenciaService();
    private final TipoRecurso agua = tipo(1L);
    private final List<Necesidad> lotes = List.of(necesidad(3L, agua, 500));

    private static TipoRecurso tipo(Long id) {
        TipoRecurso t = new TipoRecurso();
        t.setId(id);
        t.setNombre("Agua potable");
        return t;
    }

    private static Necesidad necesidad(Long id, TipoRecurso tipo, int cantidad) {
        Necesidad n = new Necesidad();
        n.setId(id);
        n.setTipoRecurso(tipo);
        n.setCantidad(cantidad);
        return n;
    }

    private Oferta oferta(int cantidad, EstadoOferta estado) {
        Oferta o = new Oferta();
        o.setTipoRecurso(agua);
        o.setCantidad(cantidad);
        o.setEstado(estado);
        return o;
    }

    private static Convocatoria convocatoria(EstadoConvocatoria estado, LocalDateTime cierre) {
        Convocatoria c = new Convocatoria();
        c.setId(7L);
        c.setEstado(estado);
        c.setFechaApertura(AHORA.minusDays(5));
        c.setFechaCierre(cierre);
        return c;
    }

    private Convocatoria vencida() {
        return convocatoria(EstadoConvocatoria.PUBLICADA, AHORA.minusHours(1));
    }

    private EtapaEmergencia etapa(Convocatoria c, Oferta... ofertas) {
        return servicio.calcularEtapa(c, List.of(ofertas), lotes, AHORA);
    }

    @Test
    void sinConvocatoriaOEnBorrador_estaEnRelevamiento() {
        assertThat(etapa(null)).isEqualTo(EtapaEmergencia.EN_RELEVAMIENTO);
        assertThat(etapa(convocatoria(EstadoConvocatoria.BORRADOR, null))).isEqualTo(EtapaEmergencia.EN_RELEVAMIENTO);
    }

    @Test
    void publicadaAntesDelCierre_estaConLaConvocatoriaAbierta() {
        Convocatoria abierta = convocatoria(EstadoConvocatoria.PUBLICADA, AHORA.plusHours(1));
        assertThat(etapa(abierta, oferta(600, EstadoOferta.PENDIENTE))).isEqualTo(EtapaEmergencia.CONVOCATORIA_ABIERTA);
    }

    @Test
    void ventanaVencidaConLotesPendientes_estaEnValidacion() {
        assertThat(etapa(vencida(), oferta(600, EstadoOferta.PENDIENTE), oferta(100, EstadoOferta.VALIDADA)))
                .isEqualTo(EtapaEmergencia.EN_VALIDACION);
    }

    @Test
    void ventanaVencidaSinLotes_requiereDecision() {
        assertThat(etapa(vencida())).isEqualTo(EtapaEmergencia.REQUIERE_DECISION);
    }

    @Test
    void loValidadoNoAlcanza_requiereDecision() {
        assertThat(etapa(vencida(), oferta(300, EstadoOferta.VALIDADA), oferta(400, EstadoOferta.NO_HABILITADA)))
                .isEqualTo(EtapaEmergencia.REQUIERE_DECISION);
    }

    @Test
    void loValidadoAlcanza_estaEnSeleccion() {
        assertThat(etapa(vencida(), oferta(300, EstadoOferta.VALIDADA), oferta(200, EstadoOferta.VALIDADA)))
                .isEqualTo(EtapaEmergencia.EN_SELECCION);
    }

    @Test
    void coberturaParcialAceptada_habilitaLaSeleccion() {
        Convocatoria c = vencida();
        c.setCoberturaParcialAceptada(true);
        assertThat(etapa(c, oferta(100, EstadoOferta.VALIDADA))).isEqualTo(EtapaEmergencia.EN_SELECCION);
    }

    @Test
    void todasNoHabilitadasConParcialAceptada_requiereDecision() {
        Convocatoria c = vencida();
        c.setCoberturaParcialAceptada(true);
        assertThat(etapa(c, oferta(600, EstadoOferta.NO_HABILITADA))).isEqualTo(EtapaEmergencia.REQUIERE_DECISION);
    }

    @Test
    void conSeleccionadasOAdjudicadasSinFinalizar_estaAdjudicada() {
        assertThat(etapa(vencida(), oferta(600, EstadoOferta.SELECCIONADA), oferta(50, EstadoOferta.NO_SELECCIONADA)))
                .isEqualTo(EtapaEmergencia.ADJUDICADA);
        assertThat(etapa(vencida(), oferta(600, EstadoOferta.ADJUDICADA))).isEqualTo(EtapaEmergencia.ADJUDICADA);
    }

    @Test
    void conAlgunaFinalizadaYOtrasAdjudicadas_estaEnEjecucion() {
        assertThat(etapa(vencida(), oferta(300, EstadoOferta.FINALIZADA), oferta(300, EstadoOferta.ADJUDICADA)))
                .isEqualTo(EtapaEmergencia.EN_EJECUCION);
    }

    @Test
    void todasLasAdjudicadasFinalizadas_estaFinalizada() {
        assertThat(etapa(vencida(), oferta(600, EstadoOferta.FINALIZADA), oferta(50, EstadoOferta.NO_SELECCIONADA)))
                .isEqualTo(EtapaEmergencia.FINALIZADA);
    }

    @Test
    void losRetiradosNoCuentan() {
        assertThat(etapa(vencida(), oferta(600, EstadoOferta.RETIRADA))).isEqualTo(EtapaEmergencia.REQUIERE_DECISION);
    }

    @Test
    void cobertura_reparteLoValidadoYAdjudicadoPorLote() {
        List<LoteDetalleDto> cobertura = servicio.cobertura(lotes,
                List.of(oferta(300, EstadoOferta.ADJUDICADA), oferta(100, EstadoOferta.NO_SELECCIONADA),
                        oferta(200, EstadoOferta.PENDIENTE), oferta(999, EstadoOferta.RETIRADA)));

        assertThat(cobertura).hasSize(1);
        assertThat(cobertura.get(0).cantidadOfertada()).isEqualTo(600L);
        assertThat(cobertura.get(0).cantidadValidada()).isEqualTo(400L);
        assertThat(cobertura.get(0).cantidadAdjudicada()).isEqualTo(300L);
        assertThat(cobertura.get(0).sinOfertado().cantidadOfertada()).isNull();
    }

    @Test
    void ventanaAbierta_exigePublicadaAperturaAlcanzadaYCierreFuturo() {
        assertThat(EtapaEmergenciaService.ventanaAbierta(convocatoria(EstadoConvocatoria.PUBLICADA, AHORA.plusHours(1)), AHORA)).isTrue();
        assertThat(EtapaEmergenciaService.ventanaAbierta(vencida(), AHORA)).isFalse();
        assertThat(EtapaEmergenciaService.ventanaAbierta(convocatoria(EstadoConvocatoria.BORRADOR, AHORA.plusHours(1)), AHORA)).isFalse();
        Convocatoria futura = convocatoria(EstadoConvocatoria.PUBLICADA, AHORA.plusDays(2));
        futura.setFechaApertura(AHORA.plusDays(1));
        assertThat(EtapaEmergenciaService.ventanaAbierta(futura, AHORA)).isFalse();
    }
}
