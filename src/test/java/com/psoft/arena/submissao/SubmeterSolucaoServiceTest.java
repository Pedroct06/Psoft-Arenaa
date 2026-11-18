package com.psoft.arena.submissao;

import com.psoft.arena.service.SubmissaoRecebidaEvent;
import com.psoft.arena.service.AcompanhamentoSubmissaoService;
import com.psoft.arena.dto.SubmissaoResponseDTO;
import com.psoft.arena.model.Atividade;
import com.psoft.arena.model.ConfiguracaoLlm;
import com.psoft.arena.model.Estrategia;
import com.psoft.arena.model.Estudante;
import com.psoft.arena.model.Problema;
import com.psoft.arena.model.Submissao;
import com.psoft.arena.model.TipoEstrategia;
import com.psoft.arena.model.Turma;
import com.psoft.arena.repository.SubmissaoRepository;
import com.psoft.arena.service.AtividadeService;
import com.psoft.arena.service.SubmeterSolucaoService;
import com.psoft.arena.service.TurmaEstudanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmeterSolucaoServiceTest {

  // "Agora" fixo: 1º/12/2026 às 12:00 UTC.
  static final Instant AGORA = Instant.parse("2026-12-01T12:00:00Z");

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

  SubmeterSolucaoService service;

  Estudante ana;
  Estudante bia;
  Atividade atividadeAberta;
  Atividade atividadeConcluida;
  Atividade atividadeExpirada;
  Atividade atividadeNoLimite;

  @BeforeEach
  void preparar() {
    Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
    service = new SubmeterSolucaoService(submissaoRepository, atividadeService,
        turmaEstudanteService, acompanhamentoService, eventPublisher, clock);

    ana = new Estudante("Ana", "123", "ana@x.com");
    ReflectionTestUtils.setField(ana, "id", 1L);

    bia = new Estudante("Bia", "456", "bia@x.com");
    ReflectionTestUtils.setField(bia, "id", 2L);

    OffsetDateTime agora = OffsetDateTime.ofInstant(AGORA, ZoneOffset.UTC);
    atividadeAberta = novaAtividade(agora.plusDays(2));
    atividadeConcluida = novaAtividade(agora.plusDays(2));
    atividadeConcluida.encerrar();
    atividadeExpirada = novaAtividade(agora.minusMinutes(1));
    atividadeNoLimite = novaAtividade(agora);
  }

  private Atividade novaAtividade(OffsetDateTime prazo) {
    return new Atividade("Ativ", "Resolva", prazo,
        new Turma("Psoft", "T1", "2026.2", "Prof"),
        new Problema("Prob", "Desc", "RF", "RP"),
        new Estrategia("Estr", TipoEstrategia.CODE_SMELLS, "D", "prompt"),
        new ConfiguracaoLlm("m", "https://a.com", "cifrada"));
  }

  @Test
  void submetePrimeiraVersaoComSucesso() {
    when(atividadeService.buscarEntidadePorId(10L)).thenReturn(atividadeAberta);
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
    when(atividadeService.buscarEntidadePorId(10L)).thenReturn(atividadeAberta);
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
  void aceitaSubmissaoNoInstanteExatoDoPrazo() {
    when(atividadeService.buscarEntidadePorId(10L)).thenReturn(atividadeNoLimite);
    when(turmaEstudanteService.listar("T1")).thenReturn(List.of(ana));
    when(submissaoRepository.countByEstudanteIdAndAtividadeId(1L, 10L)).thenReturn(0);
    when(submissaoRepository.save(any(Submissao.class))).thenAnswer(inv -> inv.getArgument(0));

    SubmissaoResponseDTO r = service.executar(1L, 10L, "Solucao.java", "public class Solucao {}");

    assertEquals(1, r.versao());
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
    when(atividadeService.buscarEntidadePorId(10L)).thenReturn(atividadeConcluida);

    assertThrows(IllegalStateException.class,
        () -> service.executar(1L, 10L, "Solucao.java", "public class Solucao {}"));

    verify(submissaoRepository, never()).save(any());
  }

  @Test
  void recusaSubmissaoAposPrazo() {
    when(atividadeService.buscarEntidadePorId(10L)).thenReturn(atividadeExpirada);

    assertThrows(IllegalStateException.class,
        () -> service.executar(1L, 10L, "Solucao.java", "public class Solucao {}"));

    verify(submissaoRepository, never()).save(any());
  }

  @Test
  void recusaEstudanteForaDaTurma() {
    when(atividadeService.buscarEntidadePorId(10L)).thenReturn(atividadeAberta);
    when(turmaEstudanteService.listar("T1")).thenReturn(List.of(ana));

    assertThrows(SecurityException.class,
        () -> service.executar(2L, 10L, "Solucao.java", "public class Solucao {}"));

    verify(submissaoRepository, never()).save(any());
  }
}
