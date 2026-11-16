package com.psoft.arena.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissoes", uniqueConstraints = @UniqueConstraint(columnNames = { "estudante_id", "atividade_id",
    "versao" }))
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

  protected Submissao() {
    // Construtor exigido pelo JPA
  }

  public Submissao(Long estudanteId, Long atividadeId, int versao, String codigoFonte) {
    this.estudanteId = estudanteId;
    this.atividadeId = atividadeId;
    this.versao = versao;
    this.codigoFonte = codigoFonte;
    this.dataHoraSubmissao = LocalDateTime.now();
    this.status = StatusSubmissao.SUBMETIDA;
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
