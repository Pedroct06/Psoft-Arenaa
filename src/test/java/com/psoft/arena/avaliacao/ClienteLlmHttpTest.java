package com.psoft.arena.avaliacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.arena.exception.FalhaLlmException;
import com.psoft.arena.model.ConfiguracaoLlm;
import com.psoft.arena.service.CriptografiaService;
import com.psoft.arena.service.llm.ClienteLlmHttp;
import com.psoft.arena.service.llm.RespostaLlm;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

// Sobe um servidor local fingindo ser a API do LLM
class ClienteLlmHttpTest {

    private static final String CHAVE_MESTRA = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private final ObjectMapper mapper = new ObjectMapper();
    private final CriptografiaService criptografia = new CriptografiaService(CHAVE_MESTRA);
    private final ClienteLlmHttp cliente = new ClienteLlmHttp(criptografia, mapper, 2);

    private HttpServer servidor;
    private final AtomicReference<String> autorizacaoRecebida = new AtomicReference<>();
    private final AtomicReference<String> corpoRecebido = new AtomicReference<>();
    private final AtomicReference<String> caminhoRecebido = new AtomicReference<>();

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.start();
    }

    @AfterEach
    void pararServidor() {
        servidor.stop(0);
    }

    private ConfiguracaoLlm modelo() {
        return new ConfiguracaoLlm("grok-3",
                "http://localhost:" + servidor.getAddress().getPort() + "/v1",
                criptografia.criptografar("chave-secreta"));
    }

    private void responder(int status, String corpo) {
        servidor.createContext("/", troca -> {
            registrar(troca);
            enviar(troca, status, corpo);
        });
    }

    private void registrar(HttpExchange troca) throws IOException {
        autorizacaoRecebida.set(troca.getRequestHeaders().getFirst("Authorization"));
        caminhoRecebido.set(troca.getRequestURI().getPath());
        corpoRecebido.set(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    private static void enviar(HttpExchange troca, int status, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        troca.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private String respostaChat(String conteudo) throws IOException {
        return mapper.writeValueAsString(Map.of("choices",
                List.of(Map.of("message", Map.of("role", "assistant", "content", conteudo)))));
    }

    @Test
    void enviaPromptComChaveDescriptografadaEInterpretaAResposta() throws IOException {
        responder(200, respostaChat(
                "{\"nota\": 7, \"justificativa\": \"Razoável.\", \"evidencias\": [\"Classe X\"]}"));

        RespostaLlm resposta = cliente.avaliar(modelo(), "PROMPT DE TESTE");

        assertEquals(7.0, resposta.nota());
        assertEquals("Razoável.", resposta.justificativa());
        assertEquals(List.of("Classe X"), resposta.evidencias());

        assertEquals("/v1/chat/completions", caminhoRecebido.get());
        assertEquals("Bearer chave-secreta", autorizacaoRecebida.get());
        JsonNode corpo = mapper.readTree(corpoRecebido.get());
        assertEquals("grok-3", corpo.get("model").asText());
        assertEquals("PROMPT DE TESTE", corpo.get("messages").get(1).get("content").asText());
    }

    @Test
    void erroDaApiResultaEmFalha() {
        responder(500, "{\"error\": \"interno\"}");

        FalhaLlmException ex = assertThrows(FalhaLlmException.class, () -> cliente.avaliar(modelo(), "p"));
        assertTrue(ex.getMessage().contains("HTTP 500"));
    }

    @Test
    void respostaEmFormatoInvalidoResultaEmFalha() throws IOException {
        responder(200, respostaChat("Achei o código bom, nota oito."));

        assertThrows(FalhaLlmException.class, () -> cliente.avaliar(modelo(), "p"));
    }

    @Test
    void corpoSemConteudoResultaEmFalha() {
        responder(200, "{\"choices\": []}");

        assertThrows(FalhaLlmException.class, () -> cliente.avaliar(modelo(), "p"));
    }

    @Test
    void modeloQueNaoRespondeNoTempoResultaEmFalha() {
        servidor.createContext("/", troca -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            enviar(troca, 200, "{}");
        });

        FalhaLlmException ex = assertThrows(FalhaLlmException.class, () -> cliente.avaliar(modelo(), "p"));
        assertTrue(ex.getMessage().contains("não respondeu"));
    }

    @Test
    void falhaDeComunicacaoResultaEmFalha() {
        int porta = servidor.getAddress().getPort();
        servidor.stop(0);
        ConfiguracaoLlm semServidor = new ConfiguracaoLlm("grok-3",
                "http://localhost:" + porta + "/v1", criptografia.criptografar("k"));

        FalhaLlmException ex = assertThrows(FalhaLlmException.class, () -> cliente.avaliar(semServidor, "p"));
        assertTrue(ex.getMessage().contains("Falha de comunicação"));
    }

    @Test
    void chaveQueNaoPodeSerRecuperadaResultaEmFalha() {
        ConfiguracaoLlm chaveCorrompida = new ConfiguracaoLlm("grok-3",
                "http://localhost:" + servidor.getAddress().getPort() + "/v1", "nao-e-uma-chave-cifrada");

        assertThrows(FalhaLlmException.class, () -> cliente.avaliar(chaveCorrompida, "p"));
    }
}
