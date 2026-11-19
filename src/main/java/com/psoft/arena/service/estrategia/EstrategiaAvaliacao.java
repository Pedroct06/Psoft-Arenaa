package com.psoft.arena.service.estrategia;

import com.psoft.arena.model.TipoEstrategia;

// Cada tipo de estratégia diz ao LLM o que verificar no código
public interface EstrategiaAvaliacao {

    TipoEstrategia tipo();

    String criterios();
}
