package com.psoft.arena.model;

import com.psoft.arena.model.estado.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

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
        this.status = StatusSubmissao.SUBMETIDA;
        this.estado = new EstadoSubmetida();
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
        estado.alterar(this, novoStatus);
    }

    public void definirEstado(EstadoSubmissao novoEstado) {
        this.estado = novoEstado;

        if (novoEstado instanceof EstadoSubmetida) {
            this.status = StatusSubmissao.SUBMETIDA;
        } else if (novoEstado instanceof EstadoEmFila) {
            this.status = StatusSubmissao.EM_FILA;
        } else if (novoEstado instanceof EstadoProcessando) {
            this.status = StatusSubmissao.PROCESSANDO;
        } else if (novoEstado instanceof EstadoConcluida) {
            this.status = StatusSubmissao.CONCLUIDA;
        } else if (novoEstado instanceof EstadoFalha) {
            this.status = StatusSubmissao.FALHA;
        }
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
