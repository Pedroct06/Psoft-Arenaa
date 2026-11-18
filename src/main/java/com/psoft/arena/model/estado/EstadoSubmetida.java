package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.model.Submissao;

public class EstadoSubmetida implements EstadoSubmissao {

    @Override
    public void alterar(Submissao submissao, StatusSubmissao novoStatus) {
        if (novoStatus != StatusSubmissao.EM_FILA) {
            throw new IllegalStateException(
                    "Transição inválida: SUBMETIDA para " + novoStatus);
        }
        submissao.definirEstado(new EstadoEmFila());
    }

    @Override
    public StatusSubmissao getStatus() {
        return StatusSubmissao.SUBMETIDA;
    }
}
