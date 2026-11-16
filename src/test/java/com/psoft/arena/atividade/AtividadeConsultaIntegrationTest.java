package com.psoft.arena.atividade;

import com.psoft.arena.model.*;
import com.psoft.arena.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:us7;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "arena.llm.chave-mestra=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
@AutoConfigureMockMvc
class AtividadeConsultaIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired TurmaRepository turmas;
    @Autowired EstudanteRepository estudantes;
    @Autowired ProblemaRepository problemas;
    @Autowired EstrategiaRepository estrategias;
    @Autowired AtividadeRepository atividades;

    String matriculaAna;
    String matriculaBia;
    Atividade daTurmaDeAna;
    Atividade daTurmaDeBia;

    @BeforeEach
    void preparar() {
        atividades.deleteAll();
        matriculaAna = UUID.randomUUID().toString();
        matriculaBia = UUID.randomUUID().toString();
        Estudante ana = estudantes.save(new Estudante("Ana", matriculaAna, "ana@x.com"));
        Estudante bia = estudantes.save(new Estudante("Bia", matriculaBia, "bia@x.com"));
        Turma t1 = new Turma("Psoft A", UUID.randomUUID().toString(), "2026.2", "Prof");
        Turma t2 = new Turma("Psoft B", UUID.randomUUID().toString(), "2026.2", "Prof");
        t1.adicionarEstudante(ana);
        t2.adicionarEstudante(bia);
        t1 = turmas.save(t1);
        t2 = turmas.save(t2);
        Problema p = problemas.save(new Problema("Problema", "Descrição", "RF", "RP"));
        Estrategia e = estrategias.save(new Estrategia("Estratégia", TipoEstrategia.CODE_SMELLS, "D", "prompt-secreto"));
        OffsetDateTime prazo = OffsetDateTime.parse("2026-12-10T23:59:00-03:00");
        ConfiguracaoLlm llm = new ConfiguracaoLlm("modelo", "https://api.exemplo.com/v1", "chave-cifrada");
        daTurmaDeAna = atividades.save(new Atividade("Ativ A", "Resolva", prazo, t1, p, e, llm));
        daTurmaDeBia = atividades.save(new Atividade("Ativ B", "Resolva", prazo, t2, p, e, llm));
    }

    @Test
    void listaSomenteAtividadesDasTurmasDoEstudante() throws Exception {
        mvc.perform(get("/estudantes/" + matriculaAna + "/atividades"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].titulo").value("Ativ A"))
            .andExpect(jsonPath("$[0].requisitosFuncionais").value("RF"))
            .andExpect(jsonPath("$[0].requisitosProjeto").value("RP"))
            .andExpect(jsonPath("$[0].estrategiaAvaliacao").value("Estratégia"))
            .andExpect(jsonPath("$[0].status").value("ABERTA"))
            .andExpect(jsonPath("$[0].prazoSubmissao").exists())
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("prompt-secreto"))))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("chave-cifrada"))));
    }

    @Test
    void consultaAtividadeDaPropriaTurma() throws Exception {
        mvc.perform(get("/estudantes/" + matriculaAna + "/atividades/" + daTurmaDeAna.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Ativ A"));
    }

    @Test
    void negaAtividadeDeTurmaDaQualNaoParticipa() throws Exception {
        mvc.perform(get("/estudantes/" + matriculaAna + "/atividades/" + daTurmaDeBia.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    void atividadeConcluidaContinuaVisivel() throws Exception {
        daTurmaDeAna.encerrar();
        atividades.save(daTurmaDeAna);
        mvc.perform(get("/estudantes/" + matriculaAna + "/atividades"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("CONCLUIDA"))
            .andExpect(jsonPath("$[0].aceitaSubmissoes").value(false));
    }

    @Test
    void estudanteOuAtividadeInexistente404() throws Exception {
        mvc.perform(get("/estudantes/nao-existe/atividades")).andExpect(status().isNotFound());
        mvc.perform(get("/estudantes/" + matriculaAna + "/atividades/999999")).andExpect(status().isNotFound());
    }
}
