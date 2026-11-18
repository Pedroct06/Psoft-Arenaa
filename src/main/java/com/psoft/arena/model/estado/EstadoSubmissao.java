package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;

public interface EstadoSubmissao {

    StatusSubmissao status();

    default EstadoSubmissao enfileirar() {
        throw transicaoInvalida(StatusSubmissao.EM_FILA);
    }

    default EstadoSubmissao iniciarProcessamento() {
        throw transicaoInvalida(StatusSubmissao.PROCESSANDO);
    }

    default EstadoSubmissao concluir() {
        throw transicaoInvalida(StatusSubmissao.CONCLUIDA);
    }

    default EstadoSubmissao falhar() {
        throw transicaoInvalida(StatusSubmissao.FALHA);
    }

    private IllegalStateException transicaoInvalida(StatusSubmissao destino) {
        return new IllegalStateException(
                "Transição inválida: " + status() + " para " + destino);
    }
}