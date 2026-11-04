package com.psoft.arena.dto;

import jakarta.validation.constraints.NotBlank;

// Na edição o código não muda (ele vem na URL).
public class TurmaUpdateDTO {

    @NotBlank(message = "Nome da turma não pode ser nulo ou vazio.")
    private String nome;

    @NotBlank(message = "Período letivo da turma não pode ser nulo ou vazio.")
    private String periodoLetivo;

    @NotBlank(message = "Professor responsável da turma não pode ser nulo ou vazio.")
    private String professorResponsavel;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getPeriodoLetivo() { return periodoLetivo; }
    public void setPeriodoLetivo(String periodoLetivo) { this.periodoLetivo = periodoLetivo; }
    public String getProfessorResponsavel() { return professorResponsavel; }
    public void setProfessorResponsavel(String professorResponsavel) { this.professorResponsavel = professorResponsavel; }
}
