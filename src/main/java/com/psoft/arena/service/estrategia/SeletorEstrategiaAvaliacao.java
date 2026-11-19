package com.psoft.arena.service.estrategia;

import com.psoft.arena.model.TipoEstrategia;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Escolhe a estratégia de avaliação pelo tipo cadastrado na atividade
@Component
public class SeletorEstrategiaAvaliacao {

    private final Map<TipoEstrategia, EstrategiaAvaliacao> estrategias = new EnumMap<>(TipoEstrategia.class);

    public SeletorEstrategiaAvaliacao(List<EstrategiaAvaliacao> disponiveis) {
        for (EstrategiaAvaliacao estrategia : disponiveis) {
            if (estrategias.put(estrategia.tipo(), estrategia) != null) {
                throw new IllegalStateException("Mais de uma estratégia para o tipo " + estrategia.tipo() + ".");
            }
        }
        // Falha ao subir a aplicação se algum tipo novo ficar sem estratégia
        for (TipoEstrategia tipo : TipoEstrategia.values()) {
            if (!estrategias.containsKey(tipo)) {
                throw new IllegalStateException("Nenhuma estratégia de avaliação para o tipo " + tipo + ".");
            }
        }
    }

    public EstrategiaAvaliacao selecionar(TipoEstrategia tipo) {
        EstrategiaAvaliacao estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new IllegalArgumentException("Tipo de estratégia não informado.");
        }
        return estrategia;
    }
}
