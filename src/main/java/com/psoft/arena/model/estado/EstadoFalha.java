package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.model.Submissao;

public class EstadoFalha implements EstadoSubmissao {

    @Override
    public void alterar(Submissao submissao, StatusSubmissao novoStatus) {
        throw new IllegalStateException("A submissão está em estado de falha.");
    }

    @Override
    public StatusSubmissao getStatus() {
        return StatusSubmissao.FALHA;
    }
}
