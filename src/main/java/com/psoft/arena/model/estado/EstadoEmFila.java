package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.model.Submissao;

public class EstadoEmFila implements EstadoSubmissao {

    @Override
    public void alterar(Submissao submissao, StatusSubmissao novoStatus) {
        if (novoStatus != StatusSubmissao.PROCESSANDO) {
            throw new IllegalStateException(
                    "Transição inválida: EM_FILA para " + novoStatus);
        }
        submissao.definirEstado(new EstadoProcessando());
    }
}
