package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;

public class EstadoSubmetida implements EstadoSubmissao {

    @Override
    public StatusSubmissao status() {
        return StatusSubmissao.SUBMETIDA;
    }

    @Override
    public EstadoSubmissao enfileirar() {
        return new EstadoEmFila();
    }
}