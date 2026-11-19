package com.psoft.arena.service.estrategia;

import com.psoft.arena.model.TipoEstrategia;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoAcoplamentoCoesao implements EstrategiaAvaliacao {

    @Override
    public TipoEstrategia tipo() {
        return TipoEstrategia.BAIXO_ACOPLAMENTO_ALTA_COESAO_E_PRINCIPIOS;
    }

    @Override
    public String criterios() {
        return """
                Avalie o acoplamento, a coesão e os princípios de projeto:
                - cada classe tem uma responsabilidade bem definida;
                - as classes dependem de interfaces e não de implementações concretas;
                - herança é usada com moderação, preferindo composição;
                - o código respeita os princípios SOLID;
                - o encapsulamento é preservado (sem expor o estado interno sem necessidade).
                Quanto mais acoplado e menos coeso, menor a nota.""";
    }
}
