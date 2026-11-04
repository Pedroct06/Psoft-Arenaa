package com.psoft.arena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    // O código identifica a turma de forma única (ex.: "PSOFT-2026.1-01").
    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String periodoLetivo;

    @Column(nullable = false)
    private String professorResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTurma status;

    protected Turma() {
        // exigido pelo JPA
    }

    public Turma(String nome, String codigo, String periodoLetivo, String professorResponsavel) {
        this.nome = nome;
        this.codigo = codigo;
        this.periodoLetivo = periodoLetivo;
        this.professorResponsavel = professorResponsavel;
        this.status = StatusTurma.ATIVA; // toda turma nasce Ativa
    }

    public void editar(String nome, String periodoLetivo, String professorResponsavel) {
        this.nome = nome;
        this.periodoLetivo = periodoLetivo;
        this.professorResponsavel = professorResponsavel;
    }

    public void encerrar() {
        this.status = StatusTurma.ENCERRADA;
    }

    public boolean isAtiva() {
        return this.status == StatusTurma.ATIVA;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCodigo() { return codigo; }
    public String getPeriodoLetivo() { return periodoLetivo; }
    public String getProfessorResponsavel() { return professorResponsavel; }
    public StatusTurma getStatus() { return status; }
}
