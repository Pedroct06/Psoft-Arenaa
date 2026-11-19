package com.psoft.arena.turma;

import com.psoft.arena.exception.ConflitoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Estudante;
import com.psoft.arena.model.Turma;
import com.psoft.arena.repository.TurmaRepository;
import com.psoft.arena.service.EstudanteService;
import com.psoft.arena.service.TurmaEstudanteService;
import com.psoft.arena.service.TurmaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurmaEstudanteServiceTest {

    @Mock
    private TurmaService turmaService;
    @Mock
    private EstudanteService estudanteService;
    @Mock
    private TurmaRepository turmaRepository;

    @InjectMocks
    private TurmaEstudanteService service;

    private Turma turma;
    private Estudante ana;

    @BeforeEach
    void setUp() {
        turma = new Turma("Projeto de Software", "PSOFT-01", "2026.2", "Prof. Fulano");
        ana = new Estudante("Ana Souza", "123", "ana@ufcg.edu.br");
    }

    @Test
    void associaEstudanteATurmaAtiva() {
        when(turmaService.garantirTurmaAtiva("PSOFT-01")).thenReturn(turma);
        when(estudanteService.buscarPorMatricula("123")).thenReturn(ana);

        service.adicionar("PSOFT-01", "123");

        assertTrue(turma.possuiEstudante(ana));
        verify(turmaRepository).save(turma);
    }

    @Test
    void naoAssociaDuasVezesOMesmoEstudante() {
        turma.adicionarEstudante(ana);
        when(turmaService.garantirTurmaAtiva("PSOFT-01")).thenReturn(turma);
        when(estudanteService.buscarPorMatricula("123")).thenReturn(ana);

        assertThrows(ConflitoException.class, () -> service.adicionar("PSOFT-01", "123"));
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void turmaEncerradaNaoRecebeAssociacao() {
        when(turmaService.garantirTurmaAtiva("PSOFT-01")).thenThrow(new ConflitoException("encerrada"));

        assertThrows(ConflitoException.class, () -> service.adicionar("PSOFT-01", "123"));
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void estudanteInexistenteLancaErro() {
        when(turmaService.garantirTurmaAtiva("PSOFT-01")).thenReturn(turma);
        when(estudanteService.buscarPorMatricula("999")).thenThrow(new RecursoNaoEncontradoException("x"));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.adicionar("PSOFT-01", "999"));
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void estudantePodeEstarEmVariasTurmas() {
        Turma outra = new Turma("Outra", "PSOFT-02", "2026.2", "Prof. Fulano");
        when(turmaService.garantirTurmaAtiva("PSOFT-01")).thenReturn(turma);
        when(turmaService.garantirTurmaAtiva("PSOFT-02")).thenReturn(outra);
        when(estudanteService.buscarPorMatricula("123")).thenReturn(ana);

        service.adicionar("PSOFT-01", "123");
        service.adicionar("PSOFT-02", "123");

        assertTrue(turma.possuiEstudante(ana));
        assertTrue(outra.possuiEstudante(ana));
    }

    @Test
    void removeEstudanteDaTurma() {
        turma.adicionarEstudante(ana);
        when(turmaService.buscarPorCodigo("PSOFT-01")).thenReturn(turma);
        when(estudanteService.buscarPorMatricula("123")).thenReturn(ana);

        service.remover("PSOFT-01", "123");

        assertFalse(turma.possuiEstudante(ana));
        verify(turmaRepository).save(turma);
    }

    @Test
    void removerEstudanteNaoAssociadoLancaErro() {
        when(turmaService.buscarPorCodigo("PSOFT-01")).thenReturn(turma);
        when(estudanteService.buscarPorMatricula("123")).thenReturn(ana);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.remover("PSOFT-01", "123"));
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void listaEstudantesDaTurma() {
        turma.adicionarEstudante(ana);
        when(turmaService.buscarPorCodigo("PSOFT-01")).thenReturn(turma);

        assertEquals(1, service.listar("PSOFT-01").size());
    }
}