package com.psoft.arena.service.estrategia;

import com.psoft.arena.model.TipoEstrategia;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoCodeSmells implements EstrategiaAvaliacao {

    @Override
    public TipoEstrategia tipo() {
        return TipoEstrategia.CODE_SMELLS;
    }

    @Override
    public String criterios() {
        return """
                Procure code smells e diga onde cada um aparece no código:
                - métodos longos ou com muitas responsabilidades;
                - classes grandes demais;
                - código duplicado;
                - listas longas de parâmetros;
                - feature envy (método que usa mais dados de outra classe do que da própria);
                - uso de tipos primitivos no lugar de objetos do domínio;
                - nomes pouco claros.
                Quanto mais smells e mais graves, menor a nota.""";
    }
}
