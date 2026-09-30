package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.exception.BonitaException;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;

@ExtendWith(MockitoExtension.class)
class EmergenciaServiceTest {

    @Mock
    private EmergenciaRepository emergenciaRepository;

    @Mock
    private BonitaService bonitaService;

    @InjectMocks
    private EmergenciaService emergenciaService;

    private Emergencia emergenciaDelCliente() {
        Emergencia e = new Emergencia();
        // El cliente intenta imponer id, caseId y fecha: el servidor debe ignorarlos
        e.setId(99L);
        e.setCaseId(555L);
        e.setFechaCreacion(LocalDate.of(2000, 1, 1));
        e.setTipoDesastre("Inundacion");
        e.setZonaAfectada("La Plata");
        return e;
    }

    @Test
    void crear_ignoraIdCaseIdYFechaDelCliente_yGuardaElCaseIdDeBonita() {
        List<Long> idsAlGuardar = new ArrayList<>();
        List<Long> caseIdsAlGuardar = new ArrayList<>();
        List<LocalDate> fechasAlGuardar = new ArrayList<>();
        // Simula la BD: al primer save asigna id 10 si viene null
        when(emergenciaRepository.save(any(Emergencia.class))).thenAnswer(inv -> {
            Emergencia e = inv.getArgument(0);
            idsAlGuardar.add(e.getId());
            caseIdsAlGuardar.add(e.getCaseId());
            fechasAlGuardar.add(e.getFechaCreacion());
            if (e.getId() == null) {
                e.setId(10L);
            }
            return e;
        });
        when(bonitaService.iniciarInstanciaEmergencia(10L, "Inundacion")).thenReturn(1234L);

        Emergencia resultado = emergenciaService.crear(emergenciaDelCliente());

        // Primer save: id y caseId en null, fecha de hoy
        assertThat(idsAlGuardar.get(0)).isNull();
        assertThat(caseIdsAlGuardar.get(0)).isNull();
        assertThat(fechasAlGuardar.get(0)).isEqualTo(LocalDate.now());
        // Resultado final
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getCaseId()).isEqualTo(1234L);
        assertThat(resultado.getFechaCreacion()).isEqualTo(LocalDate.now());
        assertThat(resultado.getTipoDesastre()).isEqualTo("Inundacion");
    }

    @Test
    void crear_llamaAIniciarInstanciaUnaSolaVezYACompletarPasosIniciales_enOrden() {
        when(emergenciaRepository.save(any(Emergencia.class))).thenAnswer(inv -> {
            Emergencia e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId(10L);
            }
            return e;
        });
        when(bonitaService.iniciarInstanciaEmergencia(10L, "Inundacion")).thenReturn(1234L);

        emergenciaService.crear(emergenciaDelCliente());

        verify(bonitaService, times(1)).iniciarInstanciaEmergencia(10L, "Inundacion");
        verify(bonitaService, times(1)).completarPasosIniciales(1234L, 10L);
        // Guarda primero (para tener id), luego Bonita, luego guarda con el caseId
        InOrder orden = inOrder(emergenciaRepository, bonitaService);
        orden.verify(emergenciaRepository).save(any(Emergencia.class));
        orden.verify(bonitaService).iniciarInstanciaEmergencia(10L, "Inundacion");
        orden.verify(bonitaService).completarPasosIniciales(1234L, 10L);
        orden.verify(emergenciaRepository).save(any(Emergencia.class));
    }

    @Test
    void crear_siBonitaFallaAlIniciarInstancia_propagaLaExcepcionYNoCompletaPasos() {
        when(emergenciaRepository.save(any(Emergencia.class))).thenAnswer(inv -> {
            Emergencia e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        when(bonitaService.iniciarInstanciaEmergencia(10L, "Inundacion"))
                .thenThrow(new BonitaException("Bonita caido"));

        assertThatThrownBy(() -> emergenciaService.crear(emergenciaDelCliente()))
                .isInstanceOf(BonitaException.class);

        verify(bonitaService, never()).completarPasosIniciales(any(), any());
        // Solo se guardo la primera vez (la del id); nunca se persistio un caseId
        verify(emergenciaRepository, times(1)).save(any(Emergencia.class));
    }

    @Test
    void crear_siFallaCompletarPasosIniciales_propagaLaExcepcionYNoGuardaElCaseId() {
        when(emergenciaRepository.save(any(Emergencia.class))).thenAnswer(inv -> {
            Emergencia e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        when(bonitaService.iniciarInstanciaEmergencia(10L, "Inundacion")).thenReturn(1234L);
        org.mockito.Mockito.doThrow(new BonitaException("tarea no aparecio"))
                .when(bonitaService).completarPasosIniciales(1234L, 10L);

        assertThatThrownBy(() -> emergenciaService.crear(emergenciaDelCliente()))
                .isInstanceOf(BonitaException.class);

        verify(emergenciaRepository, times(1)).save(any(Emergencia.class));
    }

    @Test
    void guardar_soloPersiste_yNoTocaBonita() {
        Emergencia e = new Emergencia();
        e.setId(5L);
        when(emergenciaRepository.save(e)).thenReturn(e);

        Emergencia resultado = emergenciaService.guardar(e);

        assertThat(resultado).isSameAs(e);
        verify(emergenciaRepository).save(e);
        verifyNoInteractions(bonitaService);
    }
}
