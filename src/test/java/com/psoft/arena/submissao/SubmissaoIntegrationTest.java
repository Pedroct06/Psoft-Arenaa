package com.psoft.arena.submissao;

import com.psoft.arena.model.*;
import com.psoft.arena.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:us_submissao;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "arena.llm.chave-mestra=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
@AutoConfigureMockMvc
class SubmissaoIntegrationTest {

  @Autowired
  MockMvc mvc;
  @Autowired
  SubmissaoRepository submissoes;
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

  Estudante ana;
  Estudante bia;
  Atividade daTurmaDeAna;
  Atividade atividadeExpirada;

  @BeforeEach
  void preparar() {
    // Sem deleteAll: a fila ainda pode estar processando a submissão do teste anterior.
    // Cada teste usa matrículas e turmas novas, então os dados não se misturam.
    ana = estudantes.save(new Estudante("Ana", UUID.randomUUID().toString(), "ana@x.com"));
    bia = estudantes.save(new Estudante("Bia", UUID.randomUUID().toString(), "bia@x.com"));

    Turma t1 = new Turma("Psoft A", UUID.randomUUID().toString(), "2026.2", "Prof");
    t1.adicionarEstudante(ana);
    t1 = turmas.save(t1);

    Problema p = problemas.save(new Problema("Problema", "Descrição", "RF", "RP"));
    Estrategia e = estrategias.save(new Estrategia("Estratégia", TipoEstrategia.CODE_SMELLS, "D", "prompt"));
    ConfiguracaoLlm llm = new ConfiguracaoLlm("modelo", "https://api.exemplo.com/v1", "chave-cifrada");

    OffsetDateTime prazoFuturo = OffsetDateTime.now().plusDays(5);
    OffsetDateTime prazoPassado = OffsetDateTime.now().minusMinutes(10);

    daTurmaDeAna = atividades.save(new Atividade("Ativ A", "Resolva", prazoFuturo, t1, p, e, llm));
    atividadeExpirada = atividades.save(new Atividade("Ativ Expirada", "Resolva", prazoPassado, t1, p, e, llm));
  }

  private MockMultipartFile arquivoJava(String nome, String conteudo) {
    return new MockMultipartFile(
        "arquivo",
        nome,
        "text/x-java-source",
        conteudo.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void submeteSolucaoComSucessoEIncrementaVersao() throws Exception {
    MockMultipartFile v1 = arquivoJava("Solucao.java", "public class Solucao {}");

    mvc.perform(multipart("/api/atividades/" + daTurmaDeAna.getId() + "/submissoes")
        .file(v1)
        .param("estudanteId", ana.getId().toString()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.submissaoId").exists())
        .andExpect(jsonPath("$.versao").value(1))
        .andExpect(jsonPath("$.status").exists())
        .andExpect(jsonPath("$.dataHora").exists())
        .andExpect(jsonPath("$.mensagem").value("Solução recebida com sucesso! Versão 1 registrada."));

    MockMultipartFile v2 = arquivoJava("Solucao.java", "public class Solucao { int x = 1; }");

    mvc.perform(multipart("/api/atividades/" + daTurmaDeAna.getId() + "/submissoes")
        .file(v2)
        .param("estudanteId", ana.getId().toString()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.versao").value(2))
        .andExpect(jsonPath("$.mensagem").value("Solução recebida com sucesso! Versão 2 registrada."));

    assertEquals(2, submissoes.countByEstudanteIdAndAtividadeId(ana.getId(), daTurmaDeAna.getId()));
  }

  @Test
  void recusaArquivoComExtensaoInvalida400() throws Exception {
    MockMultipartFile arquivoPython = arquivoJava("solucao.py", "print('hello')");

    mvc.perform(multipart("/api/atividades/" + daTurmaDeAna.getId() + "/submissoes")
        .file(arquivoPython)
        .param("estudanteId", ana.getId().toString()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro").value("A solução deve ser submetida em um arquivo Java (.java)."));
  }

  @Test
  void recusaArquivoVazio400() throws Exception {
    MockMultipartFile arquivoVazio = arquivoJava("Solucao.java", "   ");

    mvc.perform(multipart("/api/atividades/" + daTurmaDeAna.getId() + "/submissoes")
        .file(arquivoVazio)
        .param("estudanteId", ana.getId().toString()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro").value("O código-fonte não pode estar vazio."));
  }

  @Test
  void recusaSubmissaoEmAtividadeConcluida409() throws Exception {
    daTurmaDeAna.encerrar();
    atividades.save(daTurmaDeAna);

    mvc.perform(multipart("/api/atividades/" + daTurmaDeAna.getId() + "/submissoes")
        .file(arquivoJava("Solucao.java", "public class Solucao {}"))
        .param("estudanteId", ana.getId().toString()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro").value("Esta atividade está encerrada e não aceita novas submissões."));
  }

  @Test
  void recusaSubmissaoAposPrazo409() throws Exception {
    mvc.perform(multipart("/api/atividades/" + atividadeExpirada.getId() + "/submissoes")
        .file(arquivoJava("Solucao.java", "public class Solucao {}"))
        .param("estudanteId", ana.getId().toString()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro").value("Esta atividade está encerrada e não aceita novas submissões."));
  }

  @Test
  void recusaEstudanteQueNaoPertenceATurma403() throws Exception {
    mvc.perform(multipart("/api/atividades/" + daTurmaDeAna.getId() + "/submissoes")
        .file(arquivoJava("Solucao.java", "public class Solucao {}"))
        .param("estudanteId", bia.getId().toString()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro").value("Estudante não pertence à turma desta atividade."));
  }

  @Test
  void recusaEstudanteInexistente403() throws Exception {
    mvc.perform(multipart("/api/atividades/" + daTurmaDeAna.getId() + "/submissoes")
        .file(arquivoJava("Solucao.java", "public class Solucao {}"))
        .param("estudanteId", "999999"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro").value("Estudante não pertence à turma desta atividade."));

    assertEquals(0, submissoes.countByEstudanteIdAndAtividadeId(999999L, daTurmaDeAna.getId()));
  }

  @Test
  void atividadeInexistente404() throws Exception {
    mvc.perform(multipart("/api/atividades/999999/submissoes")
        .file(arquivoJava("Solucao.java", "public class Solucao {}"))
        .param("estudanteId", ana.getId().toString()))
        .andExpect(status().isNotFound());
  }
}
