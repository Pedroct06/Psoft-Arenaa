package com.psoft.arena.avaliacao;

import com.psoft.arena.exception.FalhaLlmException;
import com.psoft.arena.service.llm.RespostaLlm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RespostaLlmTest {

    @Test
    void interpretaJsonValido() {
        RespostaLlm resposta = RespostaLlm.interpretar("""
                {"nota": 8.5, "justificativa": "Boa coesão.", "evidencias": ["Classe Pedido coesa", "Uso de interface"]}
                """);

        assertEquals(8.5, resposta.nota());
        assertEquals("Boa coesão.", resposta.justificativa());
        assertEquals(List.of("Classe Pedido coesa", "Uso de interface"), resposta.evidencias());
    }

    @Test
    void interpretaJsonCercadoPorBlocoDeCodigo() {
        RespostaLlm resposta = RespostaLlm.interpretar("""
                Segue a avaliação:
                ```json
                {"nota": 10, "justificativa": "Excelente.", "evidencias": ["Strategy aplicado"]}
                ```
                """);

        assertEquals(10.0, resposta.nota());
    }

    @Test
    void aceitaNotasNosLimites() {
        assertEquals(0.0, RespostaLlm.interpretar(
                "{\"nota\": 0, \"justificativa\": \"j\", \"evidencias\": [\"e\"]}").nota());
        assertEquals(10.0, RespostaLlm.interpretar(
                "{\"nota\": 10, \"justificativa\": \"j\", \"evidencias\": [\"e\"]}").nota());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"nota\": 11, \"justificativa\": \"j\", \"evidencias\": [\"e\"]}",
            "{\"nota\": -1, \"justificativa\": \"j\", \"evidencias\": [\"e\"]}",
            "{\"nota\": \"oito\", \"justificativa\": \"j\", \"evidencias\": [\"e\"]}",
            "{\"justificativa\": \"j\", \"evidencias\": [\"e\"]}",
            "{\"nota\": 7, \"evidencias\": [\"e\"]}",
            "{\"nota\": 7, \"justificativa\": \"  \", \"evidencias\": [\"e\"]}",
            "{\"nota\": 7, \"justificativa\": \"j\"}",
            "{\"nota\": 7, \"justificativa\": \"j\", \"evidencias\": []}",
            "{\"nota\": 7, \"justificativa\": \"j\", \"evidencias\": [\" \"]}",
            "não sou JSON",
            "{nota: 7",
            "   "
    })
    void recusaRespostaInvalida(String texto) {
        assertThrows(FalhaLlmException.class, () -> RespostaLlm.interpretar(texto));
    }

    @Test
    void recusaRespostaNula() {
        assertThrows(FalhaLlmException.class, () -> RespostaLlm.interpretar(null));
    }
}
