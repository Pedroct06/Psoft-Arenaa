package com.psoft.arena.turma;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TurmaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BODY = "{\"nome\":\"Projeto de Software\",\"codigo\":\"PSOFT-01\","
            + "\"periodoLetivo\":\"2026.2\",\"professorResponsavel\":\"Prof. Fulano\"}";

    private void criar() throws Exception {
        mockMvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(BODY));
    }

    @Test
    void cadastraRetorna201ComStatusAtiva() throws Exception {
        mockMvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    void codigoDuplicadoRetorna409() throws Exception {
        criar();
        mockMvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void camposVaziosRetornam400() throws Exception {
        String invalido = BODY.replace("Prof. Fulano", "");
        mockMvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultaPorCodigoRetorna200() throws Exception {
        criar();
        mockMvc.perform(get("/turmas/PSOFT-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Projeto de Software"));
    }

    @Test
    void consultaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/turmas/NAO-EXISTE")).andExpect(status().isNotFound());
    }

    @Test
    void listaRetorna200() throws Exception {
        criar();
        mockMvc.perform(get("/turmas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void editaRetorna200() throws Exception {
        criar();
        String novo = "{\"nome\":\"Novo\",\"periodoLetivo\":\"2027.1\",\"professorResponsavel\":\"Outro\"}";
        mockMvc.perform(put("/turmas/PSOFT-01").contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo"))
                .andExpect(jsonPath("$.codigo").value("PSOFT-01"));
    }

    @Test
    void encerraEContinuaConsultavel() throws Exception {
        criar();
        mockMvc.perform(patch("/turmas/PSOFT-01/encerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADA"));
        mockMvc.perform(get("/turmas/PSOFT-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADA"));
    }
}
