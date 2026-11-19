package com.psoft.arena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Problema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String descricao;

    @Column(nullable = false, length = 4000)
    private String requisitosFuncionais;

    @Column(nullable = false, length = 4000)
    private String requisitosProjeto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProblema status;

    // Passa a true quando o problema é usado em alguma atividade.
    @Column(nullable = false)
    private boolean utilizado;

    protected Problema() {
        // exigido pelo JPA
    }

    public Problema(String titulo, String descricao, String requisitosFuncionais, String requisitosProjeto) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.requisitosFuncionais = requisitosFuncionais;
        this.requisitosProjeto = requisitosProjeto;
        this.status = StatusProblema.ATIVO; // todo problema nasce Ativo
        this.utilizado = false;
    }

    public void editar(String titulo, String descricao, String requisitosFuncionais, String requisitosProjeto) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.requisitosFuncionais = requisitosFuncionais;
        this.requisitosProjeto = requisitosProjeto;
    }

    public void desativar() {
        this.status = StatusProblema.INATIVO;
    }

    public void marcarComoUtilizado() {
        this.utilizado = true;
    }

    public boolean isAtivo() {
        return this.status == StatusProblema.ATIVO;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getRequisitosFuncionais() { return requisitosFuncionais; }
    public String getRequisitosProjeto() { return requisitosProjeto; }
    public StatusProblema getStatus() { return status; }
    public boolean isUtilizado() { return utilizado; }
}
