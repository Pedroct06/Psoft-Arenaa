package com.psoft.arena.submissao;

import com.psoft.arena.model.HistoricoSubmissao;
import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.model.Submissao;
import com.psoft.arena.repository.HistoricoSubmissaoRepository;
import com.psoft.arena.repository.SubmissaoRepository;
import com.psoft.arena.service.AcompanhamentoSubmissaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcompanhamentoSubmissaoServiceTest {

    @Mock
    SubmissaoRepository submissaoRepository;

    @Mock
    HistoricoSubmissaoRepository historicoRepository;

    @InjectMocks
    AcompanhamentoSubmissaoService service;

    Submissao submissao;

    @BeforeEach
    void preparar() {
        submissao = new Submissao(
                1L,
                10L,
                1,
                "public class Solucao {}"
        );
        ReflectionTestUtils.setField(submissao, "id", 100L);
    }

    @Test
    void registraEstadoInicial() {
        service.registrarEstadoInicial(submissao);

        verify(historicoRepository).save(any(HistoricoSubmissao.class));
    }

    @Test
    void alteraStatusERegistraHistorico() {
        when(submissaoRepository.findById(100L))
                .thenReturn(Optional.of(submissao));

        service.alterarStatus(100L, StatusSubmissao.EM_FILA);

        assertEquals(StatusSubmissao.EM_FILA, submissao.getStatus());
        verify(submissaoRepository).save(submissao);
        verify(historicoRepository).save(any(HistoricoSubmissao.class));
    }

    @Test
    void rejeitaTransicaoDeStatusInvalida() {
        when(submissaoRepository.findById(100L))
                .thenReturn(Optional.of(submissao));

        assertThrows(
                IllegalStateException.class,
                () -> service.alterarStatus(100L, StatusSubmissao.CONCLUIDA)
        );

        verify(submissaoRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void consultaHistoricoDaSubmissao() {
        List<HistoricoSubmissao> historico = List.of(
                new HistoricoSubmissao(
                        100L,
                        null,
                        StatusSubmissao.SUBMETIDA
                )
        );

        when(historicoRepository.findBySubmissaoIdOrderByDataHoraAsc(100L))
                .thenReturn(historico);

        List<HistoricoSubmissao> resultado =
                service.consultarHistorico(100L);

        assertEquals(1, resultado.size());
        assertEquals(StatusSubmissao.SUBMETIDA, resultado.get(0).getStatusNovo());
    }
}
