package com.psoft.arena.dto;

import com.psoft.arena.model.Estudante;

public class EstudanteResponseDTO {

    private final String nome;
    private final String matricula;
    private final String email;

    public EstudanteResponseDTO(Estudante estudante) {
        this.nome = estudante.getNome();
        this.matricula = estudante.getMatricula();
        this.email = estudante.getEmail();
    }

    public String getNome() { return nome; }
    public String getMatricula() { return matricula; }
    public String getEmail() { return email; }
}
