package com.psoft.arena.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.arena.exception.FalhaLlmException;
import com.psoft.arena.model.ConfiguracaoLlm;
import com.psoft.arena.service.CriptografiaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

// Formato de chat completions usado pelo Grok (https://api.x.ai/v1)
@Component
public class ClienteLlmHttp implements ClienteLlm {

    private static final String ENDPOINT_CHAT = "/chat/completions";

    private final CriptografiaService criptografia;
    private final ObjectMapper mapper;
    private final Duration timeout;
    private final HttpClient http;

    public ClienteLlmHttp(CriptografiaService criptografia, ObjectMapper mapper,
            @Value("${arena.llm.timeout-segundos:60}") long timeoutSegundos) {
        this.criptografia = criptografia;
        this.mapper = mapper;
        this.timeout = Duration.ofSeconds(timeoutSegundos);
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(timeoutSegundos, 10)))
                .build();
    }

    @Override
    public RespostaLlm avaliar(ConfiguracaoLlm modelo, String prompt) {
        HttpRequest requisicao = HttpRequest.newBuilder(endpoint(modelo.getUrlApi()))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + chaveDe(modelo))
                .POST(HttpRequest.BodyPublishers.ofString(corpo(modelo, prompt)))
                .build();

        HttpResponse<String> resposta = enviar(requisicao, modelo);

        if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
            throw new FalhaLlmException("A API do modelo " + modelo.getNomeModelo()
                    + " retornou erro HTTP " + resposta.statusCode() + ".");
        }
        return RespostaLlm.interpretar(conteudoDa(resposta.body()));
    }

    private HttpResponse<String> enviar(HttpRequest requisicao, ConfiguracaoLlm modelo) {
        try {
            return http.send(requisicao, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException ex) {
            throw new FalhaLlmException("O modelo " + modelo.getNomeModelo()
                    + " não respondeu em " + timeout.toSeconds() + " segundos.");
        } catch (IOException ex) {
            throw new FalhaLlmException("Falha de comunicação com o modelo "
                    + modelo.getNomeModelo() + ": " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new FalhaLlmException("A chamada ao modelo " + modelo.getNomeModelo()
                    + " foi interrompida.");
        }
    }

    private String chaveDe(ConfiguracaoLlm modelo) {
        try {
            return criptografia.descriptografar(modelo.getChaveApiCriptografada());
        } catch (IllegalStateException ex) {
            throw new FalhaLlmException("Não foi possível recuperar a chave de acesso do modelo "
                    + modelo.getNomeModelo() + ".");
        }
    }

    private String corpo(ConfiguracaoLlm modelo, String prompt) {
        Map<String, Object> corpo = Map.of(
                "model", modelo.getNomeModelo(),
                "temperature", 0,
                "messages", List.of(
                        Map.of("role", "system", "content", PromptAvaliacao.INSTRUCAO_SISTEMA),
                        Map.of("role", "user", "content", prompt)));
        try {
            return mapper.writeValueAsString(corpo);
        } catch (JsonProcessingException ex) {
            throw new FalhaLlmException("Não foi possível montar a requisição ao modelo.");
        }
    }

    private String conteudoDa(String corpoResposta) {
        try {
            JsonNode conteudo = mapper.readTree(corpoResposta)
                    .path("choices").path(0).path("message").path("content");
            return conteudo.isTextual() ? conteudo.asText() : null;
        } catch (JsonProcessingException ex) {
            throw new FalhaLlmException("A API do modelo retornou um corpo que não é JSON.");
        }
    }

    private static URI endpoint(String urlApi) {
        String url = urlApi.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.endsWith(ENDPOINT_CHAT)) {
            url = url + ENDPOINT_CHAT;
        }
        try {
            return URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw new FalhaLlmException("URL da API do modelo inválida.");
        }
    }
}
