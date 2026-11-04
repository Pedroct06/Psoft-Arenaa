package com.psoft.arena.problema;

import com.psoft.arena.dto.ProblemaRequestDTO;
import com.psoft.arena.model.Problema;
import com.psoft.arena.service.ProblemaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProblemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProblemaService service;

    private static final String BODY = "{\"titulo\":\"Biblioteca\",\"descricao\":\"Gerenciar empréstimos\","
            + "\"requisitosFuncionais\":\"Cadastrar livros\",\"requisitosProjeto\":\"Usar Spring\"}";

    private Problema criar() {
        ProblemaRequestDTO dto = new ProblemaRequestDTO();
        dto.setTitulo("Biblioteca");
        dto.setDescricao("Gerenciar empréstimos");
        dto.setRequisitosFuncionais("Cadastrar livros");
        dto.setRequisitosProjeto("Usar Spring");
        return service.cadastrar(dto);
    }

    @Test
    void cadastraRetorna201ComStatusAtivo() throws Exception {
        mockMvc.perform(post("/problemas").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.utilizado").value(false));
    }

    @Test
    void camposVaziosRetornam400() throws Exception {
        String invalido = "{\"titulo\":\"\",\"descricao\":\"x\",\"requisitosFuncionais\":\"x\",\"requisitosProjeto\":\"x\"}";
        mockMvc.perform(post("/problemas").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultaPorIdRetorna200() throws Exception {
        Long id = criar().getId();
        mockMvc.perform(get("/problemas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Biblioteca"));
    }

    @Test
    void consultaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/problemas/999999")).andExpect(status().isNotFound());
    }

    @Test
    void listaRetorna200() throws Exception {
        criar();
        mockMvc.perform(get("/problemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void editaRetorna200() throws Exception {
        Long id = criar().getId();
        String novo = "{\"titulo\":\"Novo\",\"descricao\":\"d\",\"requisitosFuncionais\":\"f\",\"requisitosProjeto\":\"p\"}";
        mockMvc.perform(put("/problemas/" + id).contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Novo"))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void desativaRetornaInativo() throws Exception {
        Long id = criar().getId();
        mockMvc.perform(patch("/problemas/" + id + "/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void removeNaoUtilizadoRetorna204() throws Exception {
        Long id = criar().getId();
        mockMvc.perform(delete("/problemas/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/problemas/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void removerUtilizadoRetorna409() throws Exception {
        Long id = criar().getId();
        service.utilizar(id);
        mockMvc.perform(delete("/problemas/" + id)).andExpect(status().isConflict());
    }
}
