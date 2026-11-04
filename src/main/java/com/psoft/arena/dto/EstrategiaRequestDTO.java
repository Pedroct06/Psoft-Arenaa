package com.psoft.arena.dto;

import com.psoft.arena.model.TipoEstrategia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Usado no cadastro e na edição (inclusive para definir/alterar o prompt).
public class EstrategiaRequestDTO {

    @NotBlank(message = "Nome da estratégia não pode ser nulo ou vazio.")
    private String nome;

    @NotNull(message = "Tipo da estratégia não pode ser nulo.")
    private TipoEstrategia tipo;

    @NotBlank(message = "Descrição da estratégia não pode ser nula ou vazia.")
    private String descricao;

    @NotBlank(message = "Prompt de avaliação da estratégia não pode ser nulo ou vazio.")
    private String promptAvaliacao;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public TipoEstrategia getTipo() { return tipo; }
    public void setTipo(TipoEstrategia tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getPromptAvaliacao() { return promptAvaliacao; }
    public void setPromptAvaliacao(String promptAvaliacao) { this.promptAvaliacao = promptAvaliacao; }
}
