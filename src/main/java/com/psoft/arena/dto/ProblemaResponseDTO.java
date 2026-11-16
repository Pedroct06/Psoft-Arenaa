package com.psoft.arena.dto;

import com.psoft.arena.model.Problema;
import com.psoft.arena.model.StatusProblema;

public class ProblemaResponseDTO {

    private final Long id;
    private final String titulo;
    private final String descricao;
    private final String requisitosFuncionais;
    private final String requisitosProjeto;
    private final StatusProblema status;
    private final boolean utilizado;

    public ProblemaResponseDTO(Problema problema) {
        this.id = problema.getId();
        this.titulo = problema.getTitulo();
        this.descricao = problema.getDescricao();
        this.requisitosFuncionais = problema.getRequisitosFuncionais();
        this.requisitosProjeto = problema.getRequisitosProjeto();
        this.status = problema.getStatus();
        this.utilizado = problema.isUtilizado();
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getRequisitosFuncionais() { return requisitosFuncionais; }
    public String getRequisitosProjeto() { return requisitosProjeto; }
    public StatusProblema getStatus() { return status; }
    public boolean isUtilizado() { return utilizado; }
}
