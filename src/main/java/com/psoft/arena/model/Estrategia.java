package com.psoft.arena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Estrategia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEstrategia tipo;

    @Column(nullable = false, length = 4000)
    private String descricao;

    // Orienta a avaliação feita por esta estratégia.
    @Column(nullable = false, length = 8000)
    private String promptAvaliacao;

    protected Estrategia() {
        // exigido pelo JPA
    }

    public Estrategia(String nome, TipoEstrategia tipo, String descricao, String promptAvaliacao) {
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
        this.promptAvaliacao = promptAvaliacao;
    }

    public void editar(String nome, TipoEstrategia tipo, String descricao, String promptAvaliacao) {
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
        this.promptAvaliacao = promptAvaliacao;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public TipoEstrategia getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public String getPromptAvaliacao() { return promptAvaliacao; }
}
