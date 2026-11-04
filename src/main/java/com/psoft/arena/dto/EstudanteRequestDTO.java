package com.psoft.arena.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EstudanteRequestDTO {

    @NotBlank(message = "Nome do estudante não pode ser nulo ou vazio.")
    private String nome;

    @NotBlank(message = "Matrícula do estudante não pode ser nula ou vazia.")
    private String matricula;

    @NotBlank(message = "E-mail do estudante não pode ser nulo ou vazio.")
    @Email(message = "E-mail do estudante é inválido.")
    private String email;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
