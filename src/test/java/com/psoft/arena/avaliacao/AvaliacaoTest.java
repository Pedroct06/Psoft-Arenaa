package com.psoft.arena.avaliacao;

import com.psoft.arena.model.Avaliacao;
import com.psoft.arena.model.ResultadoModelo;
import com.psoft.arena.service.llm.RespostaLlm;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AvaliacaoTest {

    private ResultadoModelo sucesso(String modelo, double nota, String justificativa, String... evidencias) {
        return ResultadoModelo.sucesso(1L, modelo, new RespostaLlm(nota, justificativa, List.of(evidencias)));
    }

    @Test
    void comUmModeloUsaONotaJustificativaEEvidenciasDele() {
        Avaliacao avaliacao = Avaliacao.compor(1L, List.of(
                sucesso("grok-3", 7.5, "Bom uso de interfaces.", "Interface Pagamento", "Sem herança desnecessária")));

        assertEquals(1L, avaliacao.getSubmissaoId());
        assertEquals(7.5, avaliacao.getNota());
        assertEquals("Bom uso de interfaces.", avaliacao.getJustificativa());
        assertEquals(List.of("Interface Pagamento", "Sem herança desnecessária"), avaliacao.getEvidencias());
        assertNotNull(avaliacao.getDataHora());
    }

    @Test
    void comVariosModelosUsaAMediaEIdentificaCadaModelo() {
        Avaliacao avaliacao = Avaliacao.compor(1L, List.of(
                sucesso("grok-3", 8.0, "Justificativa A.", "Evidência A"),
                sucesso("grok-3-mini", 5.0, "Justificativa B.", "Evidência B")));

        assertEquals(6.5, avaliacao.getNota());
        assertTrue(avaliacao.getJustificativa().contains("[grok-3] Justificativa A."));
        assertTrue(avaliacao.getJustificativa().contains("[grok-3-mini] Justificativa B."));
        assertEquals(List.of("[grok-3] Evidência A", "[grok-3-mini] Evidência B"), avaliacao.getEvidencias());
    }

    @Test
    void arredondaAMediaParaDuasCasas() {
        Avaliacao avaliacao = Avaliacao.compor(1L, List.of(
                sucesso("a", 10, "j", "e"),
                sucesso("b", 10, "j", "e"),
                sucesso("c", 9, "j", "e")));

        assertEquals(9.67, avaliacao.getNota());
    }

    @Test
    void naoCompoeSeAlgumModeloFalhou() {
        List<ResultadoModelo> resultados = List.of(
                sucesso("grok-3", 8.0, "j", "e"),
                ResultadoModelo.falha(1L, "grok-3-mini", "não respondeu"));

        assertThrows(IllegalArgumentException.class, () -> Avaliacao.compor(1L, resultados));
    }

    @Test
    void naoCompoeSemResultados() {
        assertThrows(IllegalArgumentException.class, () -> Avaliacao.compor(1L, List.of()));
    }
}
