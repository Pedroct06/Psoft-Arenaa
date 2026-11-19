package com.psoft.arena.service.llm;

import com.psoft.arena.model.Estrategia;
import com.psoft.arena.model.Problema;
import com.psoft.arena.service.estrategia.EstrategiaAvaliacao;

public final class PromptAvaliacao {

    public static final String INSTRUCAO_SISTEMA = """
            Você é um avaliador de soluções de projeto de software orientado a objetos em Java.
            Avalie a solução exclusivamente por análise estática do código-fonte: não execute o código.
            Responda somente com um objeto JSON, sem texto adicional, no formato:
            {"nota": <número de 0 a 10>, "justificativa": "<justificativa da nota>", \
            "evidencias": ["<trecho ou observação do código que sustenta a avaliação>", "..."]}""";

    private PromptAvaliacao() {
    }

    public static String montar(Problema problema, Estrategia estrategia,
                                EstrategiaAvaliacao estrategiaAvaliacao, String codigoFonte) {
        return """
                ## Estratégia de avaliação: %s (%s)
                %s

                ## Orientações do professor
                %s

                ## Problema: %s
                %s

                ## Requisitos funcionais
                %s

                ## Requisitos de projeto
                %s

                ## Código-fonte submetido
                ```java
                %s
                ```

                Avalie o código considerando os requisitos funcionais, os requisitos de projeto \
                e os critérios da estratégia acima. A nota deve ir de 0 a 10.
                """.formatted(
                estrategia.getNome(),
                estrategia.getTipo(),
                estrategiaAvaliacao.criterios(),
                estrategia.getPromptAvaliacao(),
                problema.getTitulo(),
                problema.getDescricao(),
                problema.getRequisitosFuncionais(),
                problema.getRequisitosProjeto(),
                codigoFonte);
    }
}
