package com.psoft.arena.atividade;

import com.psoft.arena.model.*;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtividadePrazoTest {

    private static final OffsetDateTime PRAZO = OffsetDateTime.parse("2026-12-10T23:59:00-03:00");

    private Atividade novaAtividade() {
        return new Atividade("Ativ", "Resolva", PRAZO,
                new Turma("Psoft", "T1", "2026.2", "Prof"),
                new Problema("Prob", "Desc", "RF", "RP"),
                new Estrategia("Estr", TipoEstrategia.CODE_SMELLS, "D", "prompt"),
                new ConfiguracaoLlm("m", "https://a.com", "cifrada"));
    }

    @Test
    void abertaAntesDoPrazoAceita() {
        assertTrue(novaAtividade().aceitaSubmissoes(PRAZO.minusMinutes(1)));
    }

    @Test
    void abertaNoInstanteDoPrazoAindaAceita() {
        assertTrue(novaAtividade().aceitaSubmissoes(PRAZO));
    }

    @Test
    void abertaDepoisDoPrazoNaoAceita() {
        assertFalse(novaAtividade().aceitaSubmissoes(PRAZO.plusSeconds(1)));
    }

    @Test
    void concluidaAntesDoPrazoNaoAceita() {
        Atividade atividade = novaAtividade();
        atividade.encerrar();
        assertFalse(atividade.aceitaSubmissoes(PRAZO.minusDays(1)));
    }
}
