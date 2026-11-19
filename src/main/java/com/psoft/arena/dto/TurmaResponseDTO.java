package com.psoft.arena.dto;

import com.psoft.arena.model.StatusTurma;
import com.psoft.arena.model.Turma;

public class TurmaResponseDTO {

    private final Long id;
    private final String nome;
    private final String codigo;
    private final String periodoLetivo;
    private final String professorResponsavel;
    private final StatusTurma status;

    public TurmaResponseDTO(Turma turma) {
        this.id = turma.getId();
        this.nome = turma.getNome();
        this.codigo = turma.getCodigo();
        this.periodoLetivo = turma.getPeriodoLetivo();
        this.professorResponsavel = turma.getProfessorResponsavel();
        this.status = turma.getStatus();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCodigo() { return codigo; }
    public String getPeriodoLetivo() { return periodoLetivo; }
    public String getProfessorResponsavel() { return professorResponsavel; }
    public StatusTurma getStatus() { return status; }
}
