package com.psoft.arena.estrategia;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EstrategiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BODY = "{\"nome\":\"Long Method\",\"tipo\":\"CODE_SMELLS\","
            + "\"descricao\":\"Procura métodos longos\",\"promptAvaliacao\":\"Aponte métodos longos.\"}";

    private int criar() throws Exception {
        String resposta = mockMvc.perform(post("/estrategias").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(resposta, "$.id");
    }

    @Test
    void cadastraRetorna201() throws Exception {
        mockMvc.perform(post("/estrategias").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CODE_SMELLS"));
    }

    @Test
    void tipoInvalidoRetorna400() throws Exception {
        String invalido = BODY.replace("CODE_SMELLS", "TIPO_QUE_NAO_EXISTE");
        mockMvc.perform(post("/estrategias").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void promptVazioRetorna400() throws Exception {
        String invalido = BODY.replace("Aponte métodos longos.", "");
        mockMvc.perform(post("/estrategias").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void variasEstrategiasDoMesmoTipoSaoPermitidas() throws Exception {
        criar();
        criar();
        mockMvc.perform(get("/estrategias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void consultaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/estrategias/999999")).andExpect(status().isNotFound());
    }

    @Test
    void alteraPromptRetorna200() throws Exception {
        int id = criar();
        String novo = BODY.replace("Aponte métodos longos.", "Novo prompt.");
        mockMvc.perform(put("/estrategias/" + id).contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptAvaliacao").value("Novo prompt."));
    }
}
