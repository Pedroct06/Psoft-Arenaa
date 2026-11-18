package com.psoft.arena.model;

import com.psoft.arena.model.estado.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "submissoes", uniqueConstraints = @UniqueConstraint(columnNames = {
        "estudante_id", "atividade_id", "versao"
}))
public class Submissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudante_id", nullable = false)
    private Long estudanteId;

    @Column(name = "atividade_id", nullable = false)
    private Long atividadeId;

    @Column(nullable = false)
    private int versao;

    @Lob
    @Column(name = "codigo_fonte", nullable = false, columnDefinition = "TEXT")
    private String codigoFonte;

    @Column(name = "data_hora_submissao", nullable = false)
    private LocalDateTime dataHoraSubmissao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSubmissao status;

    @Transient
    private EstadoSubmissao estado;

    protected Submissao() {
    }

    public Submissao(Long estudanteId, Long atividadeId, int versao, String codigoFonte) {
        this.estudanteId = estudanteId;
        this.atividadeId = atividadeId;
        this.versao = versao;
        this.codigoFonte = codigoFonte;
        this.dataHoraSubmissao = LocalDateTime.now();
        definirEstado(new EstadoSubmetida());
    }

    @PostLoad
    private void restaurarEstado() {
        this.estado = switch (status) {
            case SUBMETIDA -> new EstadoSubmetida();
            case EM_FILA -> new EstadoEmFila();
            case PROCESSANDO -> new EstadoProcessando();
            case CONCLUIDA -> new EstadoConcluida();
            case FALHA -> new EstadoFalha();
        };
    }

    public void alterarStatus(StatusSubmissao novoStatus) {
        if (estado == null) {
            restaurarEstado();
        }

        Objects.requireNonNull(novoStatus, "O status solicitado não pode ser nulo.");

        EstadoSubmissao proximo = switch (novoStatus) {
            case EM_FILA -> estado.enfileirar();
            case PROCESSANDO -> estado.iniciarProcessamento();
            case CONCLUIDA -> estado.concluir();
            case FALHA -> estado.falhar();
            case SUBMETIDA -> throw new IllegalStateException(
                    "SUBMETIDA é o estado inicial.");
        };

        definirEstado(proximo);
    }

    private void definirEstado(EstadoSubmissao novoEstado) {
        Objects.requireNonNull(novoEstado, "O estado não pode ser nulo.");

        this.status = novoEstado.status();
        this.estado = novoEstado;
    }

    public Long getId() {
        return id;
    }

    public Long getEstudanteId() {
        return estudanteId;
    }

    public Long getAtividadeId() {
        return atividadeId;
    }

    public int getVersao() {
        return versao;
    }

    public String getCodigoFonte() {
        return codigoFonte;
    }

    public LocalDateTime getDataHoraSubmissao() {
        return dataHoraSubmissao;
    }

    public StatusSubmissao getStatus() {
        return status;
    }
}