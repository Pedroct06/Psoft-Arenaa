package com.psoft.arena.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submissao_id", nullable = false, unique = true)
    private Long submissaoId;

    @Column(nullable = false)
    private double nota;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String evidencias;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    protected Avaliacao() {
    }

    private Avaliacao(Long submissaoId, double nota, String justificativa, String evidencias) {
        this.submissaoId = submissaoId;
        this.nota = nota;
        this.justificativa = justificativa;
        this.evidencias = evidencias;
        this.dataHora = LocalDateTime.now();
    }

    // Nota final = média dos modelos
    public static Avaliacao compor(Long submissaoId, List<ResultadoModelo> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            throw new IllegalArgumentException("A avaliação precisa de ao menos um resultado de modelo.");
        }
        if (resultados.stream().anyMatch(r -> !r.isSucesso())) {
            throw new IllegalArgumentException("Todos os modelos devem produzir resultados válidos.");
        }

        boolean variosModelos = resultados.size() > 1;
        double media = resultados.stream().mapToDouble(ResultadoModelo::getNota).average().orElseThrow();

        List<String> justificativas = new ArrayList<>();
        List<String> evidencias = new ArrayList<>();
        for (ResultadoModelo resultado : resultados) {
            String prefixo = variosModelos ? "[" + resultado.getNomeModelo() + "] " : "";
            justificativas.add(prefixo + resultado.getJustificativa());
            resultado.getEvidencias().forEach(e -> evidencias.add(prefixo + e));
        }

        return new Avaliacao(
                submissaoId,
                arredondar(media),
                String.join("\n\n", justificativas),
                String.join(ResultadoModelo.SEPARADOR_EVIDENCIAS, evidencias));
    }

    private static double arredondar(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public Long getId() { return id; }
    public Long getSubmissaoId() { return submissaoId; }
    public double getNota() { return nota; }
    public String getJustificativa() { return justificativa; }
    public LocalDateTime getDataHora() { return dataHora; }

    public List<String> getEvidencias() {
        return Arrays.asList(evidencias.split(ResultadoModelo.SEPARADOR_EVIDENCIAS));
    }
}
