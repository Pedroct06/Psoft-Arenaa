package com.psoft.arena.service;

import com.psoft.arena.exception.AvaliacaoFalhouException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Atividade;
import com.psoft.arena.model.Avaliacao;
import com.psoft.arena.model.ConfiguracaoLlm;
import com.psoft.arena.model.ResultadoModelo;
import com.psoft.arena.model.Submissao;
import com.psoft.arena.repository.AtividadeRepository;
import com.psoft.arena.repository.AvaliacaoRepository;
import com.psoft.arena.repository.ResultadoModeloRepository;
import com.psoft.arena.repository.SubmissaoRepository;
import com.psoft.arena.service.estrategia.EstrategiaAvaliacao;
import com.psoft.arena.service.estrategia.SeletorEstrategiaAvaliacao;
import com.psoft.arena.service.llm.ClienteLlm;
import com.psoft.arena.service.llm.PromptAvaliacao;
import com.psoft.arena.service.llm.RespostaLlm;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Sem @Transactional: a chamada ao LLM pode demorar e não deve segurar o banco
@Service
public class AvaliacaoService {

    private final SubmissaoRepository submissaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final ResultadoModeloRepository resultadoModeloRepository;
    private final ClienteLlm clienteLlm;
    private final SeletorEstrategiaAvaliacao seletorEstrategia;

    public AvaliacaoService(
            SubmissaoRepository submissaoRepository,
            AtividadeRepository atividadeRepository,
            AvaliacaoRepository avaliacaoRepository,
            ResultadoModeloRepository resultadoModeloRepository,
            ClienteLlm clienteLlm,
            SeletorEstrategiaAvaliacao seletorEstrategia) {
        this.submissaoRepository = submissaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.resultadoModeloRepository = resultadoModeloRepository;
        this.clienteLlm = clienteLlm;
        this.seletorEstrategia = seletorEstrategia;
    }

    // Se qualquer modelo falhar, a avaliação não é concluída
    public Avaliacao avaliar(Long submissaoId) {
        Submissao submissao = submissaoRepository.findById(submissaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Submissão com ID " + submissaoId + " não encontrada."));
        Atividade atividade = atividadeRepository.findParaAvaliacaoById(submissao.getAtividadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Atividade com ID " + submissao.getAtividadeId() + " não encontrada."));

        EstrategiaAvaliacao estrategiaAvaliacao =
                seletorEstrategia.selecionar(atividade.getEstrategia().getTipo());
        String prompt = PromptAvaliacao.montar(atividade.getProblema(), atividade.getEstrategia(),
                estrategiaAvaliacao, submissao.getCodigoFonte());

        List<ResultadoModelo> resultados = atividade.getModelosAvaliacao().stream()
                .map(modelo -> avaliarComModelo(submissaoId, modelo, prompt))
                .toList();

        List<ResultadoModelo> falhas = resultados.stream().filter(r -> !r.isSucesso()).toList();
        if (!falhas.isEmpty()) {
            throw new AvaliacaoFalhouException("Avaliação da submissão " + submissaoId
                    + " não concluída. " + falhas.stream()
                            .map(f -> f.getNomeModelo() + ": " + f.getErro())
                            .collect(Collectors.joining("; ")));
        }

        return avaliacaoRepository.save(Avaliacao.compor(submissaoId, resultados));
    }

    private ResultadoModelo avaliarComModelo(Long submissaoId, ConfiguracaoLlm modelo, String prompt) {
        ResultadoModelo resultado;
        try {
            RespostaLlm resposta = clienteLlm.avaliar(modelo, prompt);
            if (resposta == null) {
                throw new IllegalStateException("O modelo não produziu resultado.");
            }
            resultado = ResultadoModelo.sucesso(submissaoId, modelo.getNomeModelo(), resposta);
        } catch (RuntimeException ex) {
            String motivo = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            resultado = ResultadoModelo.falha(submissaoId, modelo.getNomeModelo(), motivo);
        }
        return resultadoModeloRepository.save(resultado);
    }
}
