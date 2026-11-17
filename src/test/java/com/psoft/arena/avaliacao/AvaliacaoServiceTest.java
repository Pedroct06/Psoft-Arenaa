package com.psoft.arena.avaliacao;

import com.psoft.arena.exception.AvaliacaoFalhouException;
import com.psoft.arena.exception.FalhaLlmException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.*;
import com.psoft.arena.repository.AtividadeRepository;
import com.psoft.arena.repository.AvaliacaoRepository;
import com.psoft.arena.repository.ResultadoModeloRepository;
import com.psoft.arena.repository.SubmissaoRepository;
import com.psoft.arena.service.AvaliacaoService;
import com.psoft.arena.service.llm.ClienteLlm;
import com.psoft.arena.service.llm.RespostaLlm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock SubmissaoRepository submissaoRepository;
    @Mock AtividadeRepository atividadeRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock ResultadoModeloRepository resultadoModeloRepository;
    @Mock ClienteLlm clienteLlm;
    @InjectMocks AvaliacaoService service;

    Submissao submissao;
    Atividade atividade;
    ConfiguracaoLlm llmPadrao;

    @BeforeEach
    void preparar() {
        Problema problema = new Problema("Loja", "Sistema de loja",
                "RF1: cadastrar pedidos", "RP1: usar padrão Strategy no frete");
        Estrategia estrategia = new Estrategia("Padrões", TipoEstrategia.CODE_SMELLS,
                "Avalia padrões", "Verifique se o padrão Strategy foi aplicado corretamente.");
        llmPadrao = new ConfiguracaoLlm("grok-3", "https://api.x.ai/v1", "chave-cifrada");
        Turma turma = new Turma("Psoft", "T1", "2026.2", "Prof");
        atividade = new Atividade("Ativ", "Resolva", OffsetDateTime.now().plusDays(1),
                turma, problema, estrategia, llmPadrao);
        ReflectionTestUtils.setField(atividade, "id", 20L);

        submissao = new Submissao(5L, 20L, 1, "public class Frete {}");
        ReflectionTestUtils.setField(submissao, "id", 10L);

        lenient().when(submissaoRepository.findById(10L)).thenReturn(Optional.of(submissao));
        lenient().when(atividadeRepository.findParaAvaliacaoById(20L)).thenReturn(Optional.of(atividade));
        lenient().when(resultadoModeloRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(avaliacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void avaliaComOLlmPadraoEGravaAvaliacaoFinal() {
        when(clienteLlm.avaliar(eq(llmPadrao), anyString()))
                .thenReturn(new RespostaLlm(9.0, "Strategy bem aplicado.", List.of("Interface CalculoFrete")));

        Avaliacao avaliacao = service.avaliar(10L);

        assertEquals(10L, avaliacao.getSubmissaoId());
        assertEquals(9.0, avaliacao.getNota());
        assertEquals("Strategy bem aplicado.", avaliacao.getJustificativa());
        assertEquals(List.of("Interface CalculoFrete"), avaliacao.getEvidencias());
        verify(avaliacaoRepository).save(avaliacao);
    }

    @Test
    void registraOResultadoDoModelo() {
        when(clienteLlm.avaliar(any(), anyString()))
                .thenReturn(new RespostaLlm(6.0, "Ok.", List.of("e1")));

        service.avaliar(10L);

        ArgumentCaptor<ResultadoModelo> captor = ArgumentCaptor.forClass(ResultadoModelo.class);
        verify(resultadoModeloRepository).save(captor.capture());
        ResultadoModelo resultado = captor.getValue();
        assertTrue(resultado.isSucesso());
        assertEquals("grok-3", resultado.getNomeModelo());
        assertEquals(10L, resultado.getSubmissaoId());
        assertEquals(6.0, resultado.getNota());
    }

    @Test
    void promptConsideraRequisitosEstrategiaECodigo() {
        when(clienteLlm.avaliar(any(), anyString()))
                .thenReturn(new RespostaLlm(6.0, "Ok.", List.of("e1")));

        service.avaliar(10L);

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(clienteLlm).avaliar(eq(llmPadrao), prompt.capture());
        assertTrue(prompt.getValue().contains("RF1: cadastrar pedidos"));
        assertTrue(prompt.getValue().contains("RP1: usar padrão Strategy no frete"));
        assertTrue(prompt.getValue().contains("Verifique se o padrão Strategy foi aplicado corretamente."));
        assertTrue(prompt.getValue().contains("public class Frete {}"));
    }

    @Test
    void falhaDoModeloImpedeAvaliacaoERegistraOErro() {
        when(clienteLlm.avaliar(any(), anyString()))
                .thenThrow(new FalhaLlmException("A API do modelo grok-3 retornou erro HTTP 500."));

        AvaliacaoFalhouException ex = assertThrows(AvaliacaoFalhouException.class, () -> service.avaliar(10L));

        assertTrue(ex.getMessage().contains("HTTP 500"));
        verify(avaliacaoRepository, never()).save(any());
        ArgumentCaptor<ResultadoModelo> captor = ArgumentCaptor.forClass(ResultadoModelo.class);
        verify(resultadoModeloRepository).save(captor.capture());
        assertFalse(captor.getValue().isSucesso());
        assertEquals("A API do modelo grok-3 retornou erro HTTP 500.", captor.getValue().getErro());
    }

    @Test
    void erroInesperadoDoModeloTambemResultaEmFalha() {
        when(clienteLlm.avaliar(any(), anyString())).thenThrow(new NullPointerException());

        assertThrows(AvaliacaoFalhouException.class, () -> service.avaliar(10L));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void respostaNulaDoModeloResultaEmFalha() {
        when(clienteLlm.avaliar(any(), anyString())).thenReturn(null);

        assertThrows(AvaliacaoFalhouException.class, () -> service.avaliar(10L));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void submissaoInexistenteNaoChamaOModelo() {
        when(submissaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.avaliar(99L));
        verifyNoInteractions(clienteLlm);
    }
}
