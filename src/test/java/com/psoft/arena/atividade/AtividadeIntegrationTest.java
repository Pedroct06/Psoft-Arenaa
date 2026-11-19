package com.psoft.arena.atividade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.psoft.arena.model.*;
import com.psoft.arena.repository.*;
import com.psoft.arena.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:us6;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "arena.llm.chave-mestra=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
@AutoConfigureMockMvc
class AtividadeIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired TurmaRepository turmas;
    @Autowired ProblemaRepository problemas;
    @Autowired EstrategiaRepository estrategias;
    @Autowired AtividadeRepository atividades;
    @Autowired AtividadeService service;
    @org.springframework.boot.test.mock.mockito.SpyBean CriptografiaService criptografia;
    Turma turma;
    Problema problema;
    Estrategia estrategia;
    ObjectNode body;

    @BeforeEach void preparar() {
        turma = turmas.save(new Turma("Psoft", UUID.randomUUID().toString(), "2026.2", "Professor"));
        problema = problemas.save(new Problema("Problema", "Descrição", "RF", "RP"));
        estrategia = estrategias.save(new Estrategia("Estratégia", TipoEstrategia.CODE_SMELLS, "Descrição", "Avalie"));
        body = mapper.createObjectNode();
        body.put("titulo", "Atividade").put("descricao", "Resolva")
            .put("prazoSubmissao", "2026-12-10T23:59:00-03:00")
            .put("codigoTurma", turma.getCodigo()).put("problemaId", problema.getId())
            .put("estrategiaId", estrategia.getId());
        body.putObject("llm").put("nomeModelo", "modelo")
            .put("urlApi", "https://api.exemplo.com/v1").put("chaveApi", "segredo-de-teste");
    }
    private MvcResult criar() throws Exception {
        return mvc.perform(post("/atividades").contentType(MediaType.APPLICATION_JSON)
            .content(body.toString())).andExpect(status().isCreated()).andReturn();
    }
    private long id(MvcResult resultado) throws Exception {
        return mapper.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }
    @Test void criaAbertaProtegeChaveEMarcaProblema() throws Exception {
        MvcResult resultado = criar();
        var json = mapper.readTree(resultado.getResponse().getContentAsString());
        assertEquals("ABERTA", json.get("status").asText());
        assertFalse(resultado.getResponse().getContentAsString().contains("segredo-de-teste"));
        assertFalse(json.has("chaveApiCriptografada"));
        String cifrada = atividades.findById(id(resultado)).orElseThrow().getConfiguracaoLlm().getChaveApiCriptografada();
        assertNotEquals("segredo-de-teste", cifrada);
        assertEquals("segredo-de-teste", criptografia.descriptografar(cifrada));
        assertTrue(problemas.findById(problema.getId()).orElseThrow().isUtilizado());
        mvc.perform(get("/atividades/" + id(resultado))).andExpect(status().isOk())
            .andExpect(jsonPath("$.nomeModelo").value("modelo"));
        mvc.perform(get("/atividades")).andExpect(status().isOk());
    }
    @Test void rejeitaTurmaEncerrada() throws Exception {
        turma.encerrar(); turmas.save(turma);
        mvc.perform(post("/atividades").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
            .andExpect(status().isConflict());
        assertFalse(problemas.findById(problema.getId()).orElseThrow().isUtilizado());
    }
    @Test void rejeitaProblemaInativo() throws Exception {
        problema.desativar(); problemas.save(problema);
        mvc.perform(post("/atividades").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
            .andExpect(status().isConflict());
    }
    @Test void rejeitaEstrategiaInexistente() throws Exception {
        body.put("estrategiaId", Long.MAX_VALUE);
        mvc.perform(post("/atividades").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
            .andExpect(status().isNotFound());
    }
    @Test void rejeitaTurmaInexistente() throws Exception {
        body.put("codigoTurma", "inexistente");
        mvc.perform(post("/atividades").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
            .andExpect(status().isNotFound());
    }
    @Test void rejeitaProblemaInexistente() throws Exception {
        body.put("problemaId", Long.MAX_VALUE);
        mvc.perform(post("/atividades").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
            .andExpect(status().isNotFound());
    }
    @Test void encerraEBloqueiaNovasSubmissoes() throws Exception {
        long id = id(criar());
        assertTrue(service.garantirAtividadeAberta(id).isAberta());
        mvc.perform(patch("/atividades/" + id + "/encerrar")).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONCLUIDA"));
        assertThrows(com.psoft.arena.exception.ConflitoException.class, () -> service.garantirAtividadeAberta(id));
    }
    @Test void rejeitaCamposAusentes() throws Exception {
        body.remove("llm");
        mvc.perform(post("/atividades").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
            .andExpect(status().isBadRequest());
    }
    @Test void rejeitaUrlInsegura() throws Exception {
        ((ObjectNode) body.get("llm")).put("urlApi", "http://api.exemplo.com");
        mvc.perform(post("/atividades").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
            .andExpect(status().isBadRequest());
    }
    @Test void falhaPosteriorAoUsoDoProblemaDesfazMarcacao() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("Falha simulada"))
            .when(criptografia).criptografar(org.mockito.ArgumentMatchers.anyString());
        var dto = mapper.treeToValue(body, com.psoft.arena.dto.AtividadeRequestDTO.class);
        long antes = atividades.count();
        assertThrows(IllegalStateException.class, () -> service.cadastrar(dto));
        assertEquals(antes, atividades.count());
        assertFalse(problemas.findById(problema.getId()).orElseThrow().isUtilizado());
    }
    @Test void criptografiaUsaIvAleatorioEDetectaAlteracao() {
        String a = criptografia.criptografar("segredo");
        String b = criptografia.criptografar("segredo");
        assertNotEquals(a, b);
        byte[] bytes = java.util.Base64.getDecoder().decode(a); bytes[bytes.length - 1] ^= 1;
        assertThrows(IllegalStateException.class,
            () -> criptografia.descriptografar(java.util.Base64.getEncoder().encodeToString(bytes)));
    }
}
