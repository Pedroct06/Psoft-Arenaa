package com.psoft.arena.dto;

import com.psoft.arena.model.Estrategia;
import com.psoft.arena.model.TipoEstrategia;

public class EstrategiaResponseDTO {

    private final Long id;
    private final String nome;
    private final TipoEstrategia tipo;
    private final String descricao;
    private final String promptAvaliacao;

    public EstrategiaResponseDTO(Estrategia estrategia) {
        this.id = estrategia.getId();
        this.nome = estrategia.getNome();
        this.tipo = estrategia.getTipo();
        this.descricao = estrategia.getDescricao();
        this.promptAvaliacao = estrategia.getPromptAvaliacao();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public TipoEstrategia getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public String getPromptAvaliacao() { return promptAvaliacao; }
}
