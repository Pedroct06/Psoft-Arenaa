package com.psoft.arena.avaliacao;

import com.psoft.arena.model.*;
import com.psoft.arena.repository.*;
import com.psoft.arena.service.AcompanhamentoSubmissaoService;
import com.psoft.arena.service.RecuperadorFila;
import com.psoft.arena.service.llm.ClienteLlm;
import com.psoft.arena.service.llm.RespostaLlm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:us_recuperador_fila;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "arena.llm.chave-mestra=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
class RecuperadorFilaIntegrationTest {

  @Autowired
  RecuperadorFila recuperadorFila;
  @Autowired
  SubmissaoRepository submissoes;
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

  @MockBean
  ClienteLlm clienteLlm;

  Estudante estudante;
  Atividade atividade;

  @BeforeEach
  void preparar() {
    estudante = estudantes.save(new Estudante("Carlos", UUID.randomUUID().toString(), "carlos@x.com"));
    Turma turma = new Turma("Psoft", UUID.randomUUID().toString(), "2026.2", "Prof");
    turma.adicionarEstudante(estudante);
    turma = turmas.save(turma);

    Problema problema = problemas.save(new Problema("Loja", "Descrição", "RF", "RP"));
    Estrategia estrategia = estrategias.save(
        new Estrategia("Smells", TipoEstrategia.CODE_SMELLS, "D", "Procure code smells."));
    ConfiguracaoLlm llm = new ConfiguracaoLlm("grok-3", "https://api.x.ai/v1", "chave-cifrada");
    atividade = atividades.save(new Atividade("Ativ", "Resolva",
        OffsetDateTime.now().plusDays(3), turma, problema, estrategia, llm));
  }

  private Submissao criarSubmissaoNoStatus(int versao, StatusSubmissao statusDesejado) {
    Submissao sub = submissoes.save(
        new Submissao(estudante.getId(), atividade.getId(), versao, "public class Solucao {}"));
    acompanhamentoService.registrarEstadoInicial(sub);

    if (statusDesejado == StatusSubmissao.EM_FILA) {
      acompanhamentoService.alterarStatus(sub.getId(), StatusSubmissao.EM_FILA);
    } else if (statusDesejado == StatusSubmissao.PROCESSANDO) {
      acompanhamentoService.alterarStatus(sub.getId(), StatusSubmissao.EM_FILA);
      acompanhamentoService.alterarStatus(sub.getId(), StatusSubmissao.PROCESSANDO);
    }
    return submissoes.findById(sub.getId()).orElseThrow();
  }

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
  void recuperaSubmissoesPendentesAoIniciar() throws Exception {
    when(clienteLlm.avaliar(any(), anyString()))
        .thenReturn(new RespostaLlm(9.0, "Bom código.", List.of("Limpo")));

    Submissao emFila = criarSubmissaoNoStatus(1, StatusSubmissao.EM_FILA);
    Submissao submetida = criarSubmissaoNoStatus(2, StatusSubmissao.SUBMETIDA);
    Submissao processando = criarSubmissaoNoStatus(3, StatusSubmissao.PROCESSANDO);

    recuperadorFila.recuperar();

    assertEquals(StatusSubmissao.CONCLUIDA, aguardarStatusFinal(emFila.getId()));
    assertEquals(StatusSubmissao.CONCLUIDA, aguardarStatusFinal(submetida.getId()));
    assertEquals(StatusSubmissao.FALHA, submissoes.findById(processando.getId()).orElseThrow().getStatus());
  }
}