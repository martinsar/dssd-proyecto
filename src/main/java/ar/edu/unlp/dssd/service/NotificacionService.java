package ar.edu.unlp.dssd.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.NotificacionDto;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.Notificacion;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.OfertaConjunta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.TipoNotificacion;
import ar.edu.unlp.dssd.repository.NotificacionRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;

/** [Entrega final] Notificaciones a las ONGs */
@Service
public class NotificacionService {

    private static final DateTimeFormatter FORMATO_CIERRE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    /** Se avisa a todas las organizaciones que se publicó o se reabrió una convocatoria */
    @Transactional
    public int notificarConvocatoria(Convocatoria convocatoria, TipoNotificacion tipo) {
        Emergencia emergencia = convocatoria.getEmergencia();
        String lugar = emergencia == null ? "" : emergencia.getTipoDesastre() + " en " + emergencia.getZonaAfectada();
        String cierre = convocatoria.getFechaCierre() == null ? "" : convocatoria.getFechaCierre().format(FORMATO_CIERRE);
        String titulo;
        String mensaje;
        if (tipo == TipoNotificacion.CONVOCATORIA_REABIERTA) {
            titulo = "Convocatoria reabierta: " + lugar;
            mensaje = "La convocatoria se reabrió hasta el " + cierre + ". Revisá los lotes y actualizá tu oferta.";
        } else {
            titulo = "Nueva convocatoria: " + lugar;
            mensaje = "Hay una nueva convocatoria abierta hasta el " + cierre + ". Revisá los lotes y cargá tu oferta.";
        }
        List<Organizacion> organizaciones = organizacionRepository.findAll();
        for (Organizacion organizacion : organizaciones) {
            notificar(organizacion, tipo, titulo, mensaje, emergencia, convocatoria, null);
        }
        return organizaciones.size();
    }

    /** Se avisa a la ONG del lote si fue adjudicado o no seleccionado; devuelve false si ya estaba avisada */
    @Transactional
    public boolean notificarResultado(Oferta oferta, TipoNotificacion tipo) {
        Organizacion organizacion = oferta.getOrganizacion();
        if (organizacion == null
                || notificacionRepository.existsByOfertaIdAndOrganizacionIdAndTipo(oferta.getId(), organizacion.getId(), tipo)) {
            return false;
        }
        Convocatoria convocatoria = oferta.getConvocatoria();
        Emergencia emergencia = convocatoria == null ? null : convocatoria.getEmergencia();
        String numero = emergencia == null ? "" : String.valueOf(emergencia.getId());
        String recurso = oferta.getTipoRecurso() == null ? "" : oferta.getTipoRecurso().getNombre();
        if (tipo == TipoNotificacion.OFERTA_ADJUDICADA) {
            notificar(organizacion, tipo, "Oferta adjudicada",
                    "Tu oferta de " + oferta.getCantidad() + " " + recurso + " para la emergencia N° " + numero
                            + " fue adjudicada.",
                    emergencia, convocatoria, oferta);
        } else {
            notificar(organizacion, tipo, "Oferta no seleccionada",
                    "Tu oferta de " + recurso + " para la emergencia N° " + numero
                            + " no fue seleccionada en esta oportunidad.",
                    emergencia, convocatoria, oferta);
        }
        return true;
    }

    /** [Entrega final] Se avisa a las ONG participantes de una oferta conjunta, salvo a la creadora */
    @Transactional
    public int notificarOfertaConjunta(OfertaConjunta conjunta) {
        Organizacion duena = conjunta.getOrganizacionDuena();
        Convocatoria convocatoria = conjunta.getConvocatoria();
        Emergencia emergencia = convocatoria == null ? null : convocatoria.getEmergencia();
        String creadora = duena == null ? "Una organización" : duena.getNombre();
        String numero = emergencia == null ? "" : " para la emergencia N° " + emergencia.getId()
                + " (" + emergencia.getTipoDesastre() + " en " + emergencia.getZonaAfectada() + ")";
        String cierre = convocatoria == null || convocatoria.getFechaCierre() == null ? ""
                : " antes del " + convocatoria.getFechaCierre().format(FORMATO_CIERRE);
        String mensaje = creadora + " te sumó a la oferta conjunta N° " + conjunta.getId() + numero
                + ". Cargá tu lote" + cierre + ".";
        int enviadas = 0;
        for (Organizacion participante : conjunta.getParticipantes()) {
            if (participante == null || (duena != null && duena.getId().equals(participante.getId()))) {
                continue;
            }
            notificar(participante, TipoNotificacion.OFERTA_CONJUNTA_INVITACION,
                    "Te sumaron a una oferta conjunta", mensaje, emergencia, convocatoria, null);
            enviadas++;
        }
        return enviadas;
    }

    public Notificacion notificar(Organizacion organizacion, TipoNotificacion tipo, String titulo, String mensaje,
                                  Emergencia emergencia, Convocatoria convocatoria, Oferta oferta) {
        Notificacion notificacion = new Notificacion();
        notificacion.setOrganizacion(organizacion);
        notificacion.setTipo(tipo);
        notificacion.setTitulo(recortar(titulo, 150));
        notificacion.setMensaje(recortar(mensaje, 500));
        notificacion.setLeida(false);
        notificacion.setFechaHora(LocalDateTime.now());
        notificacion.setEmergencia(emergencia);
        notificacion.setConvocatoria(convocatoria);
        notificacion.setOferta(oferta);
        return notificacionRepository.save(notificacion);
    }

    @Transactional(readOnly = true)
    public List<NotificacionDto> listar(Long organizacionId) {
        return notificacionRepository.findTop100ByOrganizacionIdOrderByFechaHoraDescIdDesc(organizacionId).stream()
                .map(NotificacionDto::desde)
                .toList();
    }

    public long contarNoLeidas(Long organizacionId) {
        return notificacionRepository.countByOrganizacionIdAndLeidaFalse(organizacionId);
    }

    /** Se marca como leída; si es de otra organización responde 404 */
    @Transactional
    public void marcarLeida(Long organizacionId, Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findByIdAndOrganizacionId(notificacionId, organizacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada."));
        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }

    @Transactional
    public void marcarTodas(Long organizacionId) {
        notificacionRepository.marcarTodasLeidas(organizacionId);
    }

    private static String recortar(String texto, int maximo) {
        return texto == null || texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }
}
