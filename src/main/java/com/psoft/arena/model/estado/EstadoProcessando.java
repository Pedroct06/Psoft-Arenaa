package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.model.Submissao;

public class EstadoProcessando implements EstadoSubmissao {

    @Override
    public void alterar(Submissao submissao, StatusSubmissao novoStatus) {
        if (novoStatus == StatusSubmissao.CONCLUIDA) {
            submissao.definirEstado(new EstadoConcluida());
            return;
        }

        if (novoStatus == StatusSubmissao.FALHA) {
            submissao.definirEstado(new EstadoFalha());
            return;
        }

        throw new IllegalStateException(
                "Transição inválida: PROCESSANDO para " + novoStatus);
    }

    @Override
    public StatusSubmissao getStatus() {
        return StatusSubmissao.PROCESSANDO;
    }
}
