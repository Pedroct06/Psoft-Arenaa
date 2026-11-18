package com.psoft.arena.atividade;

import com.psoft.arena.dto.AtividadeEstudanteResponseDTO;
import com.psoft.arena.exception.AcessoNegadoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.*;
import com.psoft.arena.repository.AtividadeRepository;
import com.psoft.arena.service.AtividadeConsultaService;
import com.psoft.arena.service.EstudanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtividadeConsultaServiceTest {

    @Mock AtividadeRepository atividadeRepository;
    @Mock EstudanteService estudanteService;
    // Relógio fixo em 1º/12/2026, antes do prazo (10/12/2026) da atividade de teste.
    Clock clock = Clock.fixed(Instant.parse("2026-12-01T12:00:00Z"), ZoneOffset.UTC);
    AtividadeConsultaService service;

    Estudante ana;
    Estudante bia;
    Atividade atividade;

    @BeforeEach
    void preparar() {
        service = new AtividadeConsultaService(atividadeRepository, estudanteService, clock);
        ana = new Estudante("Ana", "123", "ana@x.com");
        bia = new Estudante("Bia", "456", "bia@x.com");
        Turma turma = new Turma("Psoft", "T1", "2026.2", "Prof");
        turma.adicionarEstudante(ana);
        atividade = new Atividade("Ativ", "Resolva", OffsetDateTime.parse("2026-12-10T23:59:00-03:00"),
                turma, new Problema("Prob", "Desc", "RF", "RP"),
                new Estrategia("Estr", TipoEstrategia.CODE_SMELLS, "D", "prompt"),
                new ConfiguracaoLlm("m", "https://a.com", "cifrada"));
    }

    @Test
    void listaAtividadesDasTurmasDoEstudante() {
        when(estudanteService.buscarPorMatricula("123")).thenReturn(ana);
        when(atividadeRepository.findByTurmaEstudantesMatriculaOrderByPrazoSubmissaoAsc("123"))
                .thenReturn(List.of(atividade));

        List<AtividadeEstudanteResponseDTO> r = service.listar("123");

        assertEquals(1, r.size());
        assertEquals("T1", r.get(0).codigoTurma());
        assertEquals("RF", r.get(0).requisitosFuncionais());
        assertEquals("RP", r.get(0).requisitosProjeto());
        assertTrue(r.get(0).aceitaSubmissoes());
    }

    @Test
    void atividadeConcluidaContinuaConsultavelMasNaoAceitaSubmissao() {
        atividade.encerrar();
        when(estudanteService.buscarPorMatricula("123")).thenReturn(ana);
        when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));

        AtividadeEstudanteResponseDTO r = service.buscar("123", 1L);

        assertEquals(StatusAtividade.CONCLUIDA, r.status());
        assertFalse(r.aceitaSubmissoes());
    }

    @Test
    void atividadeAbertaMasComPrazoVencidoNaoAceitaSubmissao() {
        Clock depoisDoPrazo = Clock.fixed(Instant.parse("2026-12-11T12:00:00Z"), ZoneOffset.UTC);
        AtividadeConsultaService servicoTardio =
                new AtividadeConsultaService(atividadeRepository, estudanteService, depoisDoPrazo);
        when(estudanteService.buscarPorMatricula("123")).thenReturn(ana);
        when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));

        AtividadeEstudanteResponseDTO r = servicoTardio.buscar("123", 1L);

        assertEquals(StatusAtividade.ABERTA, r.status());
        assertFalse(r.aceitaSubmissoes());
    }

    @Test
    void recusaEstudanteForaDaTurma() {
        when(estudanteService.buscarPorMatricula("456")).thenReturn(bia);
        when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));

        assertThrows(AcessoNegadoException.class, () -> service.buscar("456", 1L));
    }

    @Test
    void atividadeInexistenteLanca404() {
        when(estudanteService.buscarPorMatricula("123")).thenReturn(ana);
        when(atividadeRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar("123", 9L));
    }
}
