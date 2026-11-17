package com.psoft.arena.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_submissoes")
public class HistoricoSubmissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submissao_id", nullable = false)
    private Long submissaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior")
    private StatusSubmissao statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false)
    private StatusSubmissao statusNovo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    protected HistoricoSubmissao() {
    }

    public HistoricoSubmissao(Long submissaoId, StatusSubmissao statusAnterior,
            StatusSubmissao statusNovo) {
        this.submissaoId = submissaoId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getSubmissaoId() {
        return submissaoId;
    }

    public StatusSubmissao getStatusAnterior() {
        return statusAnterior;
    }

    public StatusSubmissao getStatusNovo() {
        return statusNovo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
