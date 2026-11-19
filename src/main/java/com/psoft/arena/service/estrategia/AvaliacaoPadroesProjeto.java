package com.psoft.arena.service.estrategia;

import com.psoft.arena.model.TipoEstrategia;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoPadroesProjeto implements EstrategiaAvaliacao {

    @Override
    public TipoEstrategia tipo() {
        return TipoEstrategia.PADROES_DE_PROJETO;
    }

    @Override
    public String criterios() {
        return """
                Avalie o uso de padrões de projeto:
                - os padrões pedidos nos requisitos de projeto foram aplicados;
                - cada padrão foi implementado corretamente, com os papéis bem definidos;
                - o padrão resolve um problema real do código e não foi usado sem necessidade;
                - aponte onde um padrão faltou e deixaria o código melhor.
                Padrão pedido e não aplicado, ou aplicado de forma errada, reduz bastante a nota.""";
    }
}
