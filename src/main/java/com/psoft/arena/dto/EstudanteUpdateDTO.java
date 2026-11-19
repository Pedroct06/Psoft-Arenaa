package com.psoft.arena.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Na edição a matrícula não muda (ela vem na URL), então só nome e e-mail.
public class EstudanteUpdateDTO {

    @NotBlank(message = "Nome do estudante não pode ser nulo ou vazio.")
    private String nome;

    @NotBlank(message = "E-mail do estudante não pode ser nulo ou vazio.")
    @Email(message = "E-mail do estudante é inválido.")
    private String email;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
