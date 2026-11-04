package com.psoft.arena.dto;

import jakarta.validation.constraints.NotBlank;

// Usado no cadastro e na edição. O status não vem do cliente: nasce Ativo e só muda por /desativar.
public class ProblemaRequestDTO {

    @NotBlank(message = "Título do problema não pode ser nulo ou vazio.")
    private String titulo;

    @NotBlank(message = "Descrição do problema não pode ser nula ou vazia.")
    private String descricao;

    @NotBlank(message = "Requisitos funcionais do problema não podem ser nulos ou vazios.")
    private String requisitosFuncionais;

    @NotBlank(message = "Requisitos de projeto do problema não podem ser nulos ou vazios.")
    private String requisitosProjeto;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getRequisitosFuncionais() { return requisitosFuncionais; }
    public void setRequisitosFuncionais(String requisitosFuncionais) { this.requisitosFuncionais = requisitosFuncionais; }
    public String getRequisitosProjeto() { return requisitosProjeto; }
    public void setRequisitosProjeto(String requisitosProjeto) { this.requisitosProjeto = requisitosProjeto; }
}
