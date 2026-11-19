package com.psoft.arena.avaliacao;

import com.psoft.arena.exception.FalhaLlmException;
import com.psoft.arena.model.*;
import com.psoft.arena.repository.*;
import com.psoft.arena.service.llm.ClienteLlm;
import com.psoft.arena.service.llm.RespostaLlm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:us_avaliacao;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "arena.llm.chave-mestra=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
@AutoConfigureMockMvc
class AvaliacaoIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired SubmissaoRepository submissoes;
    @Autowired AvaliacaoRepository avaliacoes;
    @Autowired ResultadoModeloRepository resultadosModelo;
    @Autowired HistoricoSubmissaoRepository historicos;
    @Autowired AtividadeRepository atividades;
    @Autowired TurmaRepository turmas;
    @Autowired EstudanteRepository estudantes;
    @Autowired ProblemaRepository problemas;
    @Autowired EstrategiaRepository estrategias;

    @MockBean ClienteLlm clienteLlm;

    Estudante ana;
    Atividade atividade;

    @BeforeEach
    void preparar() {
        ana = estudantes.save(new Estudante("Ana", UUID.randomUUID().toString(), "ana@x.com"));
        Turma turma = new Turma("Psoft", UUID.randomUUID().toString(), "2026.2", "Prof");
        turma.adicionarEstudante(ana);
        turma = turmas.save(turma);

        Problema problema = problemas.save(new Problema("Loja", "Descrição", "RF", "RP"));
        Estrategia estrategia = estrategias.save(
                new Estrategia("Smells", TipoEstrategia.CODE_SMELLS, "D", "Procure code smells."));
        ConfiguracaoLlm llm = new ConfiguracaoLlm("grok-3", "https://api.x.ai/v1", "chave-cifrada");
        atividade = atividades.save(new Atividade("Ativ", "Resolva",
                OffsetDateTime.now().plusDays(3), turma, problema, estrategia, llm));
    }

    private Submissao submeterEAguardarFim() throws Exception {
        mvc.perform(multipart("/api/atividades/" + atividade.getId() + "/submissoes")
                        .file(new MockMultipartFile("arquivo", "Solucao.java", "text/x-java-source",
                                "public class Solucao {}".getBytes(StandardCharsets.UTF_8)))
                        .param("estudanteId", ana.getId().toString()))
                .andExpect(status().isCreated());

        Long id = submissoes.findByEstudanteIdAndAtividadeIdOrderByVersaoAsc(ana.getId(), atividade.getId())
                .get(0).getId();
        long limite = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < limite) {
            Submissao atual = submissoes.findById(id).orElseThrow();
            if (atual.getStatus() == StatusSubmissao.CONCLUIDA || atual.getStatus() == StatusSubmissao.FALHA) {
                return atual;
            }
            Thread.sleep(50);
        }
        return fail("A submissão não terminou de ser processada a tempo.");
    }

    @Test
    void submissaoAvaliadaComSucessoTerminaConcluidaComAvaliacao() throws Exception {
        when(clienteLlm.avaliar(any(), anyString()))
                .thenReturn(new RespostaLlm(8.0, "Código limpo.", List.of("Sem métodos longos")));

        Submissao submissao = submeterEAguardarFim();

        assertEquals(StatusSubmissao.CONCLUIDA, submissao.getStatus());
        Avaliacao avaliacao = avaliacoes.findBySubmissaoId(submissao.getId()).orElseThrow();
        assertEquals(8.0, avaliacao.getNota());
        assertEquals("Código limpo.", avaliacao.getJustificativa());
        assertEquals(List.of("Sem métodos longos"), avaliacao.getEvidencias());

        List<StatusSubmissao> estados = historicos.findBySubmissaoIdOrderByDataHoraAsc(submissao.getId())
                .stream().map(HistoricoSubmissao::getStatusNovo).toList();
        assertEquals(List.of(StatusSubmissao.SUBMETIDA, StatusSubmissao.EM_FILA,
                StatusSubmissao.PROCESSANDO, StatusSubmissao.CONCLUIDA), estados);
    }

    @Test
    void falhaDoLlmTerminaEmFalhaSemAvaliacao() throws Exception {
        when(clienteLlm.avaliar(any(), anyString()))
                .thenThrow(new FalhaLlmException("O modelo grok-3 não respondeu em 60 segundos."));

        Submissao submissao = submeterEAguardarFim();

        assertEquals(StatusSubmissao.FALHA, submissao.getStatus());
        assertTrue(avaliacoes.findBySubmissaoId(submissao.getId()).isEmpty());

        List<ResultadoModelo> resultados = resultadosModelo.findBySubmissaoIdOrderByDataHoraAsc(submissao.getId());
        assertEquals(1, resultados.size());
        assertFalse(resultados.get(0).isSucesso());
        assertEquals("O modelo grok-3 não respondeu em 60 segundos.", resultados.get(0).getErro());
    }
}
