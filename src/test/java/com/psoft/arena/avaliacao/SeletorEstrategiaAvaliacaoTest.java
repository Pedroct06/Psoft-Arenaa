package com.psoft.arena.avaliacao;

import com.psoft.arena.model.TipoEstrategia;
import com.psoft.arena.service.estrategia.AvaliacaoAcoplamentoCoesao;
import com.psoft.arena.service.estrategia.AvaliacaoCodeSmells;
import com.psoft.arena.service.estrategia.AvaliacaoPadroesProjeto;
import com.psoft.arena.service.estrategia.EstrategiaAvaliacao;
import com.psoft.arena.service.estrategia.SeletorEstrategiaAvaliacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeletorEstrategiaAvaliacaoTest {

    private final SeletorEstrategiaAvaliacao seletor = new SeletorEstrategiaAvaliacao(List.of(
            new AvaliacaoCodeSmells(), new AvaliacaoAcoplamentoCoesao(), new AvaliacaoPadroesProjeto()));

    @Test
    void escolheAEstrategiaDeCadaTipo() {
        assertInstanceOf(AvaliacaoCodeSmells.class, seletor.selecionar(TipoEstrategia.CODE_SMELLS));
        assertInstanceOf(AvaliacaoAcoplamentoCoesao.class,
                seletor.selecionar(TipoEstrategia.BAIXO_ACOPLAMENTO_ALTA_COESAO_E_PRINCIPIOS));
        assertInstanceOf(AvaliacaoPadroesProjeto.class, seletor.selecionar(TipoEstrategia.PADROES_DE_PROJETO));
    }

    @ParameterizedTest
    @EnumSource(TipoEstrategia.class)
    void todoTipoTemCriteriosProprios(TipoEstrategia tipo) {
        EstrategiaAvaliacao estrategia = seletor.selecionar(tipo);

        assertEquals(tipo, estrategia.tipo());
        assertFalse(estrategia.criterios().isBlank());
    }

    @Test
    void criteriosSaoDiferentesEntreOsTipos() {
        long distintos = List.of(TipoEstrategia.values()).stream()
                .map(tipo -> seletor.selecionar(tipo).criterios())
                .distinct().count();

        assertEquals(TipoEstrategia.values().length, distintos);
    }

    @Test
    void tipoNuloEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> seletor.selecionar(null));
    }

    @Test
    void recusaQuandoFaltaEstrategiaParaAlgumTipo() {
        assertThrows(IllegalStateException.class,
                () -> new SeletorEstrategiaAvaliacao(List.of(new AvaliacaoCodeSmells())));
    }

    @Test
    void recusaDuasEstrategiasParaOMesmoTipo() {
        assertThrows(IllegalStateException.class, () -> new SeletorEstrategiaAvaliacao(List.of(
                new AvaliacaoCodeSmells(), new AvaliacaoCodeSmells(),
                new AvaliacaoAcoplamentoCoesao(), new AvaliacaoPadroesProjeto())));
    }
}
