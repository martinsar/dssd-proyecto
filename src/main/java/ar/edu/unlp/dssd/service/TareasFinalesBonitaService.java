package ar.edu.unlp.dssd.service;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ar.edu.unlp.dssd.exception.BonitaException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;

/**
 * [Entrega final] Tareas finales de Bonita ("Seleccionar ofertas" y "Finalizar actividades").
 * Con integracion-completa=false un fallo o una tarea ausente solo se registra en el log;
 * con true la operación falla (409 si la tarea no está pendiente, 502 si Bonita falla).
 */
@Service
public class TareasFinalesBonitaService {

    private static final Logger log = LoggerFactory.getLogger(TareasFinalesBonitaService.class);

    private final BonitaService bonitaService;
    private final boolean integracionCompleta;

    public TareasFinalesBonitaService(BonitaService bonitaService,
                                      @Value("${rescuesync.bonita.integracion-completa:false}") boolean integracionCompleta) {
        this.bonitaService = bonitaService;
        this.integracionCompleta = integracionCompleta;
    }

    public boolean integracionCompleta() {
        return integracionCompleta;
    }

    public void seleccionarOfertas(Long caseId) {
        ejecutar(BonitaService.TAREA_SELECCIONAR_OFERTAS, caseId, () -> bonitaService.seleccionarOfertas(caseId),
                "El proceso todavía no habilitó la selección de ofertas. Intentá nuevamente en unos minutos.");
    }

    public void finalizarActividades(Long caseId) {
        ejecutar(BonitaService.TAREA_FINALIZAR_ACTIVIDADES, caseId, () -> bonitaService.finalizarActividades(caseId),
                "El proceso todavía no habilitó el cierre de actividades. Intentá nuevamente en unos minutos.");
    }

    private void ejecutar(String tarea, Long caseId, Supplier<Boolean> llamada, String mensajeAusente) {
        if (caseId == null) {
            if (integracionCompleta) {
                throw new ReglaNegocioException("La emergencia no tiene un caso asociado en Bonita");
            }
            log.warn("La emergencia no tiene caso en Bonita; se omite la tarea '{}'", tarea);
            return;
        }
        try {
            if (!llamada.get()) {
                if (integracionCompleta) {
                    throw new ReglaNegocioException(mensajeAusente);
                }
                log.warn("Se omite la tarea '{}' del caso {}: no está pendiente", tarea, caseId);
            }
        } catch (BonitaException e) {
            if (integracionCompleta) {
                throw e;
            }
            log.warn("No se pudo ejecutar la tarea '{}' del caso {}: {}", tarea, caseId, e.getMessage());
        }
    }
}
