package com.psoft.arena.model.estado;

import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.model.Submissao;

public interface EstadoSubmissao {
    void alterar(Submissao submissao, StatusSubmissao novoStatus);

    StatusSubmissao getStatus();
}
