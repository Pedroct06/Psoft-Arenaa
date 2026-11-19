package com.psoft.arena.dto;

import jakarta.validation.constraints.NotBlank;

public class TurmaRequestDTO {

    @NotBlank(message = "Nome da turma não pode ser nulo ou vazio.")
    private String nome;

    @NotBlank(message = "Código da turma não pode ser nulo ou vazio.")
    private String codigo;

    @NotBlank(message = "Período letivo da turma não pode ser nulo ou vazio.")
    private String periodoLetivo;

    @NotBlank(message = "Professor responsável da turma não pode ser nulo ou vazio.")
    private String professorResponsavel;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getPeriodoLetivo() { return periodoLetivo; }
    public void setPeriodoLetivo(String periodoLetivo) { this.periodoLetivo = periodoLetivo; }
    public String getProfessorResponsavel() { return professorResponsavel; }
    public void setProfessorResponsavel(String professorResponsavel) { this.professorResponsavel = professorResponsavel; }
}
