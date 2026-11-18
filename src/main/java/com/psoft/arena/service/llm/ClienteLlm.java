package com.psoft.arena.service.llm;

import com.psoft.arena.model.ConfiguracaoLlm;

public interface ClienteLlm {

    // Lança FalhaLlmException se o modelo não responder ou a resposta for inválida
    RespostaLlm avaliar(ConfiguracaoLlm modelo, String prompt);
}
