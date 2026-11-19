package com.psoft.arena.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EstudanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ANA =
            "{\"nome\":\"Ana Souza\",\"matricula\":\"123\",\"email\":\"ana@ufcg.edu.br\"}";

    @Test
    void cadastraRetorna201() throws Exception {
        mockMvc.perform(post("/estudantes").contentType(MediaType.APPLICATION_JSON).content(ANA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matricula").value("123"));
    }

    @Test
    void matriculaDuplicadaRetorna409() throws Exception {
        mockMvc.perform(post("/estudantes").contentType(MediaType.APPLICATION_JSON).content(ANA))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/estudantes").contentType(MediaType.APPLICATION_JSON).content(ANA))
                .andExpect(status().isConflict());
    }

    @Test
    void dadosInvalidosRetornam400() throws Exception {
        String invalido = "{\"nome\":\"\",\"matricula\":\"1\",\"email\":\"abc\"}";
        mockMvc.perform(post("/estudantes").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultaPorMatriculaRetorna200() throws Exception {
        mockMvc.perform(post("/estudantes").contentType(MediaType.APPLICATION_JSON).content(ANA));
        mockMvc.perform(get("/estudantes/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"));
    }

    @Test
    void consultaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/estudantes/999")).andExpect(status().isNotFound());
    }

    @Test
    void listaRetorna200() throws Exception {
        mockMvc.perform(post("/estudantes").contentType(MediaType.APPLICATION_JSON).content(ANA));
        mockMvc.perform(get("/estudantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void editaRetorna200() throws Exception {
        mockMvc.perform(post("/estudantes").contentType(MediaType.APPLICATION_JSON).content(ANA));
        String novo = "{\"nome\":\"Ana Maria\",\"email\":\"ana.maria@ufcg.edu.br\"}";
        mockMvc.perform(put("/estudantes/123").contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Maria"))
                .andExpect(jsonPath("$.matricula").value("123"));
    }

    @Test
    void editarInexistenteRetorna404() throws Exception {
        String novo = "{\"nome\":\"X\",\"email\":\"x@y.com\"}";
        mockMvc.perform(put("/estudantes/999").contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isNotFound());
    }
}
