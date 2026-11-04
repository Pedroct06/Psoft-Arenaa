package com.psoft.arena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.UniqueConstraint;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(name = "turma_estudante",
            joinColumns = @JoinColumn(name = "turma_id"),
            inverseJoinColumns = @JoinColumn(name = "estudante_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"turma_id", "estudante_id"}))
    private Set<Estudante> estudantes = new HashSet<>();

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

    public boolean possuiEstudante(Estudante estudante) {
        return estudantes.stream().anyMatch(e -> e.getMatricula().equals(estudante.getMatricula()));
    }

    public void adicionarEstudante(Estudante estudante) {
        estudantes.add(estudante);
    }

    public void removerEstudante(Estudante estudante) {
        estudantes.removeIf(e -> e.getMatricula().equals(estudante.getMatricula()));
    }

    public Set<Estudante> getEstudantes() { return Collections.unmodifiableSet(estudantes); }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCodigo() { return codigo; }
    public String getPeriodoLetivo() { return periodoLetivo; }
    public String getProfessorResponsavel() { return professorResponsavel; }
    public StatusTurma getStatus() { return status; }
}