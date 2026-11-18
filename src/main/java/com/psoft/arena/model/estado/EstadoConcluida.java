package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;

public class EstadoConcluida implements EstadoSubmissao {

    @Override
    public StatusSubmissao status() {
        return StatusSubmissao.CONCLUIDA;
    }
}