package com.psoft.arena.model;

import com.psoft.arena.service.llm.RespostaLlm;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

// Guarda o que cada modelo respondeu, inclusive quando falha
@Entity
@Table(name = "resultados_modelo")
public class ResultadoModelo {

    static final String SEPARADOR_EVIDENCIAS = "\n";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submissao_id", nullable = false)
    private Long submissaoId;

    @Column(name = "nome_modelo", nullable = false)
    private String nomeModelo;

    @Column(nullable = false)
    private boolean sucesso;

    private Double nota;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String evidencias;

    @Column(length = 2000)
    private String erro;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    protected ResultadoModelo() {
    }

    private ResultadoModelo(Long submissaoId, String nomeModelo, boolean sucesso) {
        this.submissaoId = submissaoId;
        this.nomeModelo = nomeModelo;
        this.sucesso = sucesso;
        this.dataHora = LocalDateTime.now();
    }

    public static ResultadoModelo sucesso(Long submissaoId, String nomeModelo, RespostaLlm resposta) {
        ResultadoModelo resultado = new ResultadoModelo(submissaoId, nomeModelo, true);
        resultado.nota = resposta.nota();
        resultado.justificativa = resposta.justificativa();
        resultado.evidencias = String.join(SEPARADOR_EVIDENCIAS, resposta.evidencias());
        return resultado;
    }

    public static ResultadoModelo falha(Long submissaoId, String nomeModelo, String erro) {
        ResultadoModelo resultado = new ResultadoModelo(submissaoId, nomeModelo, false);
        resultado.erro = erro == null || erro.length() <= 2000 ? erro : erro.substring(0, 2000);
        return resultado;
    }

    public Long getId() { return id; }
    public Long getSubmissaoId() { return submissaoId; }
    public String getNomeModelo() { return nomeModelo; }
    public boolean isSucesso() { return sucesso; }
    public Double getNota() { return nota; }
    public String getJustificativa() { return justificativa; }
    public String getErro() { return erro; }
    public LocalDateTime getDataHora() { return dataHora; }

    public List<String> getEvidencias() {
        if (evidencias == null || evidencias.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(evidencias.split(SEPARADOR_EVIDENCIAS));
    }
}
