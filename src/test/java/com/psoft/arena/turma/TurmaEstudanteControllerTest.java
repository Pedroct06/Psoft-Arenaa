package com.psoft.arena.turma;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TurmaEstudanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String TURMA = "{\"nome\":\"Projeto de Software\",\"codigo\":\"PSOFT-01\","
            + "\"periodoLetivo\":\"2026.2\",\"professorResponsavel\":\"Prof. Fulano\"}";
    private static final String TURMA_2 = TURMA.replace("PSOFT-01", "PSOFT-02");
    private static final String ANA =
            "{\"nome\":\"Ana Souza\",\"matricula\":\"123\",\"email\":\"ana@ufcg.edu.br\"}";

    @BeforeEach
    void criarBase() throws Exception {
        mockMvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(TURMA));
        mockMvc.perform(post("/estudantes").contentType(MediaType.APPLICATION_JSON).content(ANA));
    }

    @Test
    void associaRetorna201() throws Exception {
        mockMvc.perform(post("/turmas/PSOFT-01/estudantes/123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matricula").value("123"));
    }

    @Test
    void associacaoDuplicadaRetorna409() throws Exception {
        mockMvc.perform(post("/turmas/PSOFT-01/estudantes/123")).andExpect(status().isCreated());
        mockMvc.perform(post("/turmas/PSOFT-01/estudantes/123")).andExpect(status().isConflict());
    }

    @Test
    void turmaEncerradaNaoRecebeAssociacaoRetorna409() throws Exception {
        mockMvc.perform(patch("/turmas/PSOFT-01/encerrar")).andExpect(status().isOk());
        mockMvc.perform(post("/turmas/PSOFT-01/estudantes/123")).andExpect(status().isConflict());
    }

    @Test
    void turmaInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/turmas/NAO-EXISTE/estudantes/123")).andExpect(status().isNotFound());
    }

    @Test
    void estudanteInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/turmas/PSOFT-01/estudantes/999")).andExpect(status().isNotFound());
    }

    @Test
    void estudantePodeParticiparDeVariasTurmas() throws Exception {
        mockMvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(TURMA_2));
        mockMvc.perform(post("/turmas/PSOFT-01/estudantes/123")).andExpect(status().isCreated());
        mockMvc.perform(post("/turmas/PSOFT-02/estudantes/123")).andExpect(status().isCreated());
    }

    @Test
    void listaEstudantesDaTurma() throws Exception {
        mockMvc.perform(post("/turmas/PSOFT-01/estudantes/123"));
        mockMvc.perform(get("/turmas/PSOFT-01/estudantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].matricula").value("123"));
    }

    @Test
    void removeAssociacaoRetorna204() throws Exception {
        mockMvc.perform(post("/turmas/PSOFT-01/estudantes/123"));
        mockMvc.perform(delete("/turmas/PSOFT-01/estudantes/123")).andExpect(status().isNoContent());
        mockMvc.perform(get("/turmas/PSOFT-01/estudantes"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void removerNaoAssociadoRetorna404() throws Exception {
        mockMvc.perform(delete("/turmas/PSOFT-01/estudantes/123")).andExpect(status().isNotFound());
    }
}