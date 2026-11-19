package com.psoft.arena.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.arena.exception.FalhaLlmException;

import java.util.ArrayList;
import java.util.List;

public record RespostaLlm(double nota, String justificativa, List<String> evidencias) {

    public static final double NOTA_MINIMA = 0.0;
    public static final double NOTA_MAXIMA = 10.0;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public RespostaLlm {
        if (Double.isNaN(nota) || nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new FalhaLlmException("Nota fora do intervalo de 0 a 10: " + nota);
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new FalhaLlmException("Resposta do modelo sem justificativa.");
        }
        if (evidencias == null || evidencias.isEmpty()) {
            throw new FalhaLlmException("Resposta do modelo sem evidências.");
        }
        justificativa = justificativa.trim();
        evidencias = List.copyOf(evidencias);
    }

    // Às vezes o modelo devolve o JSON dentro de ```json ou com texto em volta
    public static RespostaLlm interpretar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new FalhaLlmException("O modelo retornou uma resposta vazia.");
        }
        int inicio = texto.indexOf('{');
        int fim = texto.lastIndexOf('}');
        if (inicio < 0 || fim <= inicio) {
            throw new FalhaLlmException("A resposta do modelo não contém um objeto JSON.");
        }

        JsonNode json;
        try {
            json = MAPPER.readTree(texto.substring(inicio, fim + 1));
        } catch (JsonProcessingException ex) {
            throw new FalhaLlmException("A resposta do modelo não é um JSON válido.");
        }

        JsonNode nota = json.get("nota");
        if (nota == null || !nota.isNumber()) {
            throw new FalhaLlmException("Resposta do modelo sem nota numérica.");
        }
        JsonNode justificativa = json.get("justificativa");
        return new RespostaLlm(
                nota.asDouble(),
                justificativa == null || !justificativa.isTextual() ? null : justificativa.asText(),
                lerEvidencias(json.get("evidencias")));
    }

    private static List<String> lerEvidencias(JsonNode no) {
        List<String> evidencias = new ArrayList<>();
        if (no == null || !no.isArray()) {
            return evidencias;
        }
        for (JsonNode item : no) {
            String texto = item.isTextual() ? item.asText() : item.toString();
            String normalizado = texto.replaceAll("\\s+", " ").trim();
            if (!normalizado.isEmpty()) {
                evidencias.add(normalizado);
            }
        }
        return evidencias;
    }
}
