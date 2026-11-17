package com.psoft.arena.avaliacao;

import com.psoft.arena.exception.AvaliacaoFalhouException;
import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.service.AcompanhamentoSubmissaoService;
import com.psoft.arena.service.AvaliacaoService;
import com.psoft.arena.service.ProcessarSubmissaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessarSubmissaoServiceTest {

    @Mock AcompanhamentoSubmissaoService acompanhamentoService;
    @Mock AvaliacaoService avaliacaoService;
    @InjectMocks ProcessarSubmissaoService service;

    @Test
    void avaliacaoComSucessoConcluiASubmissao() {
        service.processar(1L);

        InOrder ordem = inOrder(acompanhamentoService, avaliacaoService);
        ordem.verify(acompanhamentoService).alterarStatus(1L, StatusSubmissao.PROCESSANDO);
        ordem.verify(avaliacaoService).avaliar(1L);
        ordem.verify(acompanhamentoService).alterarStatus(1L, StatusSubmissao.CONCLUIDA);
        verify(acompanhamentoService, never()).alterarStatus(1L, StatusSubmissao.FALHA);
    }

    @Test
    void falhaNaAvaliacaoLevaSubmissaoParaFalha() {
        when(avaliacaoService.avaliar(1L)).thenThrow(new AvaliacaoFalhouException("modelo não respondeu"));

        service.processar(1L);

        verify(acompanhamentoService).alterarStatus(1L, StatusSubmissao.PROCESSANDO);
        verify(acompanhamentoService).alterarStatus(1L, StatusSubmissao.FALHA);
        verify(acompanhamentoService, never()).alterarStatus(1L, StatusSubmissao.CONCLUIDA);
    }
}
