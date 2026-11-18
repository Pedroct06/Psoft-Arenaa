package com.psoft.arena.submissao;

import com.psoft.arena.service.SubmissaoRecebidaEvent;
import com.psoft.arena.service.AcompanhamentoSubmissaoService;
import com.psoft.arena.dto.AtividadeResponseDTO;
import com.psoft.arena.dto.SubmissaoResponseDTO;
import com.psoft.arena.model.Estudante;
import com.psoft.arena.model.StatusAtividade;
import com.psoft.arena.model.Submissao;
import com.psoft.arena.repository.SubmissaoRepository;
import com.psoft.arena.service.AtividadeService;
import com.psoft.arena.service.SubmeterSolucaoService;
import com.psoft.arena.service.TurmaEstudanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmeterSolucaoServiceTest {

  @Mock
  SubmissaoRepository submissaoRepository;
  @Mock
  AtividadeService atividadeService;
  @Mock
  TurmaEstudanteService turmaEstudanteService;
  @Mock
  AcompanhamentoSubmissaoService acompanhamentoService;
  @Mock
  ApplicationEventPublisher eventPublisher;
  @InjectMocks
  SubmeterSolucaoService service;

  Estudante ana;
  Estudante bia;
  AtividadeResponseDTO atividadeAberta;
  AtividadeResponseDTO atividadeConcluida;
  AtividadeResponseDTO atividadeExpirada;

  @BeforeEach
  void preparar() {
    ana = new Estudante("Ana", "123", "ana@x.com");
    ReflectionTestUtils.setField(ana, "id", 1L);

    bia = new Estudante("Bia", "456", "bia@x.com");
    ReflectionTestUtils.setField(bia, "id", 2L);

    atividadeAberta = mock(AtividadeResponseDTO.class);
    lenient().when(atividadeAberta.status()).thenReturn(StatusAtividade.ABERTA);
    lenient().when(atividadeAberta.prazoSubmissao()).thenReturn(OffsetDateTime.now().plusDays(2));
    lenient().when(atividadeAberta.codigoTurma()).thenReturn("T1");

    atividadeConcluida = mock(AtividadeResponseDTO.class);
    lenient().when(atividadeConcluida.status()).thenReturn(StatusAtividade.CONCLUIDA);
    lenient().when(atividadeConcluida.prazoSubmissao()).thenReturn(OffsetDateTime.now().plusDays(2));

    atividadeExpirada = mock(AtividadeResponseDTO.class);
    lenient().when(atividadeExpirada.status()).thenReturn(StatusAtividade.ABERTA);
    lenient().when(atividadeExpirada.prazoSubmissao()).thenReturn(OffsetDateTime.now().minusMinutes(1));
  }

  @Test
  void submetePrimeiraVersaoComSucesso() {
    when(atividadeService.buscarPorId(10L)).thenReturn(atividadeAberta);
    when(turmaEstudanteService.listar("T1")).thenReturn(List.of(ana));
    when(submissaoRepository.countByEstudanteIdAndAtividadeId(1L, 10L)).thenReturn(0);
    when(submissaoRepository.save(any(Submissao.class))).thenAnswer(inv -> {
      Submissao s = inv.getArgument(0);
      ReflectionTestUtils.setField(s, "id", 100L);
      return s;
    });

    SubmissaoResponseDTO r = service.executar(1L, 10L, "Solucao.java", "public class Solucao {}");

    assertEquals(100L, r.submissaoId());
    assertEquals(1, r.versao());
    assertNotNull(r.status());
    assertNotNull(r.dataHora());
    assertEquals("Solução recebida com sucesso! Versão 1 registrada.", r.mensagem());
    verify(acompanhamentoService).registrarEstadoInicial(any(Submissao.class));
    verify(eventPublisher).publishEvent(any(SubmissaoRecebidaEvent.class));
  }

  @Test
  void incrementaVersaoQuandoJaExistemSubmissoesAnteriores() {
    when(atividadeService.buscarPorId(10L)).thenReturn(atividadeAberta);
    when(turmaEstudanteService.listar("T1")).thenReturn(List.of(ana));
    when(submissaoRepository.countByEstudanteIdAndAtividadeId(1L, 10L)).thenReturn(2);
    when(submissaoRepository.save(any(Submissao.class))).thenAnswer(inv -> {
      Submissao s = inv.getArgument(0);
      ReflectionTestUtils.setField(s, "id", 103L);
      return s;
    });

    SubmissaoResponseDTO r = service.executar(1L, 10L, "Solucao.java", "public class Solucao {}");

    assertEquals(3, r.versao());
    assertEquals("Solução recebida com sucesso! Versão 3 registrada.", r.mensagem());
    verify(acompanhamentoService).registrarEstadoInicial(any(Submissao.class));
    verify(eventPublisher).publishEvent(any(SubmissaoRecebidaEvent.class)); 
  }

  @Test
  void recusaArquivoQueNaoSejaJava() {
    assertThrows(IllegalArgumentException.class,
        () -> service.executar(1L, 10L, "Solucao.py", "print('oi')"));
    assertThrows(IllegalArgumentException.class,
        () -> service.executar(1L, 10L, null, "public class Solucao {}"));

    verifyNoInteractions(atividadeService, submissaoRepository);
  }

  @Test
  void recusaCodigoFonteVazioOuNulo() {
    assertThrows(IllegalArgumentException.class,
        () -> service.executar(1L, 10L, "Solucao.java", "   "));
    assertThrows(IllegalArgumentException.class,
        () -> service.executar(1L, 10L, "Solucao.java", null));

    verifyNoInteractions(atividadeService, submissaoRepository);
  }

  @Test
  void recusaSubmissaoEmAtividadeConcluida() {
    when(atividadeService.buscarPorId(10L)).thenReturn(atividadeConcluida);

    assertThrows(IllegalStateException.class,
        () -> service.executar(1L, 10L, "Solucao.java", "public class Solucao {}"));

    verify(submissaoRepository, never()).save(any());
  }

  @Test
  void recusaSubmissaoAposPrazo() {
    when(atividadeService.buscarPorId(10L)).thenReturn(atividadeExpirada);

    assertThrows(IllegalStateException.class,
        () -> service.executar(1L, 10L, "Solucao.java", "public class Solucao {}"));

    verify(submissaoRepository, never()).save(any());
  }

  @Test
  void recusaEstudanteForaDaTurma() {
    when(atividadeService.buscarPorId(10L)).thenReturn(atividadeAberta);
    when(turmaEstudanteService.listar("T1")).thenReturn(List.of(ana));

    assertThrows(SecurityException.class,
        () -> service.executar(2L, 10L, "Solucao.java", "public class Solucao {}"));

    verify(submissaoRepository, never()).save(any());
  }
}
