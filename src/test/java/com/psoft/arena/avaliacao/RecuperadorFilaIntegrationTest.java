package com.psoft.arena.avaliacao;

import com.psoft.arena.model.*;
import com.psoft.arena.repository.*;
import com.psoft.arena.service.AcompanhamentoSubmissaoService;
import com.psoft.arena.service.RecuperadorFila;
import com.psoft.arena.service.llm.ClienteLlm;
import com.psoft.arena.service.llm.RespostaLlm;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:us_recuperador_fila_completo;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "arena.llm.chave-mestra=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
class RecuperadorFilaIntegrationTest {

  static Long idBootEmFila1;
  static Long idBootSubmetida2;
  static Long idBootProcessando3;
  static Long idEstudanteBoot;
  static final List<String> ordemExecucaoBoot = Collections.synchronizedList(new ArrayList<>());
  static final ByteArrayOutputStream saidaTerminalBoot = new ByteArrayOutputStream();
  static PrintStream stdoutOriginalBoot;

  @TestConfiguration
  static class SementeAntesDoStartupConfig {

    @MockBean
    ClienteLlm clienteLlm;

    @Bean
    Runnable semearBancoAntesDoApplicationReady(
        EstudanteRepository estudantes,
        TurmaRepository turmas,
        ProblemaRepository problemas,
        EstrategiaRepository estrategias,
        AtividadeRepository atividades,
        SubmissaoRepository submissoes,
        AcompanhamentoSubmissaoService acompanhamentoService) {

      return new Runnable() {
        @PostConstruct
        @Override
        public void run() {
          stdoutOriginalBoot = System.out;
          System.setOut(new PrintStream(saidaTerminalBoot, true, StandardCharsets.UTF_8));

          when(clienteLlm.avaliar(any(), anyString())).thenAnswer(invocation -> {
            String prompt = invocation.getArgument(1, String.class);
            if (prompt.contains("CODIGO_SUB_1"))
              ordemExecucaoBoot.add("SUB_1");
            if (prompt.contains("CODIGO_SUB_2"))
              ordemExecucaoBoot.add("SUB_2");
            if (prompt.contains("CODIGO_SUB_3"))
              ordemExecucaoBoot.add("SUB_3");
            return new RespostaLlm(9.5, "Avaliado no startup.", List.of("Ok"));
          });

          Estudante est = estudantes.save(new Estudante("BootUser", UUID.randomUUID().toString(), "boot@x.com"));
          idEstudanteBoot = est.getId();

          Turma turma = new Turma("Psoft", UUID.randomUUID().toString(), "2026.2", "Prof");
          turma.adicionarEstudante(est);
          turma = turmas.save(turma);

          Problema problema = problemas.save(new Problema("Loja", "Descrição", "RF", "RP"));
          Estrategia estrategia = estrategias.save(
              new Estrategia("Smells", TipoEstrategia.CODE_SMELLS, "D", "Analise."));
          ConfiguracaoLlm llm = new ConfiguracaoLlm("grok-3", "https://api.x.ai/v1", "chave-cifrada");
          Atividade ativ = atividades.save(new Atividade("Ativ Boot", "Resolva",
              OffsetDateTime.now().plusDays(3), turma, problema, estrategia, llm));

          // 1ª submissão (menor ID): estava EM_FILA
          Submissao s1 = submissoes.save(new Submissao(est.getId(), ativ.getId(), 1, "CODIGO_SUB_1"));
          acompanhamentoService.registrarEstadoInicial(s1);
          acompanhamentoService.alterarStatus(s1.getId(), StatusSubmissao.EM_FILA);
          idBootEmFila1 = s1.getId();

          // 2ª submissão (ID intermediário): estava SUBMETIDA
          Submissao s2 = submissoes.save(new Submissao(est.getId(), ativ.getId(), 2, "CODIGO_SUB_2"));
          acompanhamentoService.registrarEstadoInicial(s2);
          idBootSubmetida2 = s2.getId();

          // 3ª submissão (maior ID): estava PROCESSANDO (queda no meio da avaliação)
          Submissao s3 = submissoes.save(new Submissao(est.getId(), ativ.getId(), 3, "CODIGO_SUB_3"));
          acompanhamentoService.registrarEstadoInicial(s3);
          acompanhamentoService.alterarStatus(s3.getId(), StatusSubmissao.EM_FILA);
          acompanhamentoService.alterarStatus(s3.getId(), StatusSubmissao.PROCESSANDO);
          idBootProcessando3 = s3.getId();

          // Limpa o buffer antes do ApplicationReadyEvent disparar o RecuperadorFila
          saidaTerminalBoot.reset();
        }
      };
    }
  }

  @Autowired
  RecuperadorFila recuperadorFila;
  @Autowired
  SubmissaoRepository submissoes;
  @Autowired
  HistoricoSubmissaoRepository historicos;
  @Autowired
  AcompanhamentoSubmissaoService acompanhamentoService;
  @Autowired
  AtividadeRepository atividades;
  @Autowired
  TurmaRepository turmas;
  @Autowired
  EstudanteRepository estudantes;
  @Autowired
  ProblemaRepository problemas;
  @Autowired
  EstrategiaRepository estrategias;
  @Autowired
  ClienteLlm clienteLlm;

  private StatusSubmissao aguardarStatusFinal(Long submissaoId) throws InterruptedException {
    long limite = System.currentTimeMillis() + 10_000;
    while (System.currentTimeMillis() < limite) {
      StatusSubmissao status = submissoes.findById(submissaoId).orElseThrow().getStatus();
      if (status == StatusSubmissao.CONCLUIDA || status == StatusSubmissao.FALHA) {
        return status;
      }
      Thread.sleep(50);
    }
    return fail("A submissão " + submissaoId + " não atingiu um estado final a tempo.");
  }

  @Test
  void applicationReadyEventDisparaRecuperacaoAutomaticaComOrdenacaoHistoricoENotificacoes() throws Exception {
    try {
      // 1. Aguarda todas as threads do startup terminarem ANTES de ler o terminal e
      // restaurar System.out
      assertEquals(StatusSubmissao.CONCLUIDA, aguardarStatusFinal(idBootEmFila1));
      assertEquals(StatusSubmissao.CONCLUIDA, aguardarStatusFinal(idBootSubmetida2));
      assertEquals(StatusSubmissao.FALHA, submissoes.findById(idBootProcessando3).orElseThrow().getStatus());

      // 5. Recuperação de falhas: PROCESSANDO nunca foi enviada ao LLM
      assertFalse(ordemExecucaoBoot.contains("SUB_3"));

      // 4. Ordenação: encaminhadas e processadas na ordem crescente de ID (SUB_1
      // antes de SUB_2)
      assertEquals(List.of("SUB_1", "SUB_2"), ordemExecucaoBoot);

      // 2. Histórico completo de cada cenário
      List<StatusSubmissao> histEmFila = historicos.findBySubmissaoIdOrderByDataHoraAsc(idBootEmFila1)
          .stream().map(HistoricoSubmissao::getStatusNovo).toList();
      assertEquals(List.of(
          StatusSubmissao.SUBMETIDA,
          StatusSubmissao.EM_FILA,
          StatusSubmissao.PROCESSANDO,
          StatusSubmissao.CONCLUIDA), histEmFila);

      List<StatusSubmissao> histSubmetida = historicos.findBySubmissaoIdOrderByDataHoraAsc(idBootSubmetida2)
          .stream().map(HistoricoSubmissao::getStatusNovo).toList();
      assertEquals(List.of(
          StatusSubmissao.SUBMETIDA,
          StatusSubmissao.EM_FILA,
          StatusSubmissao.PROCESSANDO,
          StatusSubmissao.CONCLUIDA), histSubmetida);

      List<StatusSubmissao> histProcessando = historicos.findBySubmissaoIdOrderByDataHoraAsc(idBootProcessando3)
          .stream().map(HistoricoSubmissao::getStatusNovo).toList();
      assertEquals(List.of(
          StatusSubmissao.SUBMETIDA,
          StatusSubmissao.EM_FILA,
          StatusSubmissao.PROCESSANDO,
          StatusSubmissao.FALHA), histProcessando);

      // 3. Notificações no terminal para cada mudança de estado
      String logTerminal = saidaTerminalBoot.toString(StandardCharsets.UTF_8);
      assertTrue(logTerminal.contains(
          "Submissão " + idBootProcessando3 + " | Estudante " + idEstudanteBoot + " | Status: PROCESSANDO -> FALHA"));
      assertTrue(logTerminal.contains(
          "Submissão " + idBootSubmetida2 + " | Estudante " + idEstudanteBoot + " | Status: SUBMETIDA -> EM_FILA"));
      assertTrue(logTerminal.contains(
          "Submissão " + idBootEmFila1 + " | Estudante " + idEstudanteBoot + " | Status: EM_FILA -> PROCESSANDO"));
      assertTrue(logTerminal.contains(
          "Submissão " + idBootEmFila1 + " | Estudante " + idEstudanteBoot + " | Status: PROCESSANDO -> CONCLUIDA"));
      assertTrue(logTerminal.contains(
          "Submissão " + idBootSubmetida2 + " | Estudante " + idEstudanteBoot + " | Status: EM_FILA -> PROCESSANDO"));
      assertTrue(logTerminal.contains(
          "Submissão " + idBootSubmetida2 + " | Estudante " + idEstudanteBoot + " | Status: PROCESSANDO -> CONCLUIDA"));
    } finally {
      if (stdoutOriginalBoot != null) {
        System.setOut(stdoutOriginalBoot);
        stdoutOriginalBoot = null;
      }
    }
  }

  @Test
  void garanteOrdenacaoCrescenteDeIdMesmoQuandoSubmetidaTemIdMenorQueEmFila() throws Exception {
    // Garante que as submissões do startup já terminaram caso este teste rode
    // primeiro
    aguardarStatusFinal(idBootEmFila1);
    aguardarStatusFinal(idBootSubmetida2);

    List<String> ordemExecucao = Collections.synchronizedList(new ArrayList<>());

    reset(clienteLlm);
    when(clienteLlm.avaliar(any(), anyString())).thenAnswer(invocation -> {
      String prompt = invocation.getArgument(1, String.class);
      if (prompt.contains("ORDEM_PRIMEIRA"))
        ordemExecucao.add("PRIMEIRA");
      if (prompt.contains("ORDEM_SEGUNDA"))
        ordemExecucao.add("SEGUNDA");
      return new RespostaLlm(8.0, "Ok.", List.of("Ok"));
    });

    Estudante est = estudantes.save(new Estudante("Ana", UUID.randomUUID().toString(), "ana@x.com"));
    Turma turma = new Turma("Psoft", UUID.randomUUID().toString(), "2026.2", "Prof");
    turma.adicionarEstudante(est);
    turma = turmas.save(turma);

    Problema problema = problemas.save(new Problema("Loja", "Descrição", "RF", "RP"));
    Estrategia estrategia = estrategias.save(
        new Estrategia("Smells", TipoEstrategia.CODE_SMELLS, "D", "Analise."));
    ConfiguracaoLlm llm = new ConfiguracaoLlm("grok-3", "https://api.x.ai/v1", "chave-cifrada");
    Atividade ativ = atividades.save(new Atividade("Ativ Ordem", "Resolva",
        OffsetDateTime.now().plusDays(3), turma, problema, estrategia, llm));

    // Criada primeiro (ID menor), mas ficou parada em SUBMETIDA
    Submissao primeiraSubmetida = submissoes.save(
        new Submissao(est.getId(), ativ.getId(), 1, "ORDEM_PRIMEIRA"));
    acompanhamentoService.registrarEstadoInicial(primeiraSubmetida);

    // Criada depois (ID maior), mas já tinha avançado para EM_FILA
    Submissao segundaEmFila = submissoes.save(
        new Submissao(est.getId(), ativ.getId(), 2, "ORDEM_SEGUNDA"));
    acompanhamentoService.registrarEstadoInicial(segundaEmFila);
    acompanhamentoService.alterarStatus(segundaEmFila.getId(), StatusSubmissao.EM_FILA);

    PrintStream stdoutOriginal = System.out;
    ByteArrayOutputStream terminalTeste = new ByteArrayOutputStream();
    System.setOut(new PrintStream(terminalTeste, true, StandardCharsets.UTF_8));

    try {
      recuperadorFila.recuperar();

      assertEquals(StatusSubmissao.CONCLUIDA, aguardarStatusFinal(primeiraSubmetida.getId()));
      assertEquals(StatusSubmissao.CONCLUIDA, aguardarStatusFinal(segundaEmFila.getId()));

      assertEquals(List.of("PRIMEIRA", "SEGUNDA"), ordemExecucao);

      String saida = terminalTeste.toString(StandardCharsets.UTF_8);
      assertTrue(saida.contains(
          "Submissão " + primeiraSubmetida.getId() + " | Estudante " + est.getId()
              + " | Status: SUBMETIDA -> EM_FILA"));
    } finally {
      System.setOut(stdoutOriginal);
    }
  }
}