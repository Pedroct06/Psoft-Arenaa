package com.psoft.arena.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
public class Atividade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String titulo;
    @Column(nullable = false, length = 4000)
    private String descricao;
    @Column(nullable = false)
    private OffsetDateTime prazoSubmissao;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private StatusAtividade status;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problema_id", nullable = false)
    private Problema problema;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estrategia_id", nullable = false)
    private Estrategia estrategia;
    @Embedded
    private ConfiguracaoLlm configuracaoLlm;

    protected Atividade() { }
    public Atividade(String titulo, String descricao, OffsetDateTime prazoSubmissao,
                     Turma turma, Problema problema, Estrategia estrategia, ConfiguracaoLlm configuracaoLlm) {
        this.titulo = titulo; this.descricao = descricao; this.prazoSubmissao = prazoSubmissao;
        this.turma = turma; this.problema = problema; this.estrategia = estrategia;
        this.configuracaoLlm = configuracaoLlm; this.status = StatusAtividade.ABERTA;
    }
    public void encerrar() { status = StatusAtividade.CONCLUIDA; }
    public boolean isAberta() { return status == StatusAtividade.ABERTA; }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public OffsetDateTime getPrazoSubmissao() { return prazoSubmissao; }
    public StatusAtividade getStatus() { return status; }
    public Turma getTurma() { return turma; }
    public Problema getProblema() { return problema; }
    public Estrategia getEstrategia() { return estrategia; }
    public ConfiguracaoLlm getConfiguracaoLlm() { return configuracaoLlm; }
    // Por enquanto só o LLM padrão; os adicionais entram na US18
    public List<ConfiguracaoLlm> getModelosAvaliacao() { return List.of(configuracaoLlm); }
}
