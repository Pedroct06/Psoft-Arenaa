package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;

public class EstadoProcessando implements EstadoSubmissao {

    @Override
    public StatusSubmissao status() {
        return StatusSubmissao.PROCESSANDO;
    }

    @Override
    public EstadoSubmissao concluir() {
        return new EstadoConcluida();
    }

    @Override
    public EstadoSubmissao falhar() {
        return new EstadoFalha();
    }
}