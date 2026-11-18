package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;

public class EstadoEmFila implements EstadoSubmissao {

    @Override
    public StatusSubmissao status() {
        return StatusSubmissao.EM_FILA;
    }

    @Override
    public EstadoSubmissao iniciarProcessamento() {
        return new EstadoProcessando();
    }
}