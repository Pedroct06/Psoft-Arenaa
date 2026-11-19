package com.psoft.arena.service;

import com.psoft.arena.exception.AcessoNegadoException;
import com.psoft.arena.exception.ConflitoException;
import org.springframework.context.ApplicationEventPublisher;
import com.psoft.arena.dto.SubmissaoResponseDTO;
import com.psoft.arena.model.Atividade;
import com.psoft.arena.model.Submissao;
import com.psoft.arena.repository.SubmissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class SubmeterSolucaoService {

    private final SubmissaoRepository submissaoRepository;
    private final AtividadeService atividadeService;
    private final TurmaEstudanteService turmaEstudanteService;
    private final AcompanhamentoSubmissaoService acompanhamentoService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public SubmeterSolucaoService(
            SubmissaoRepository submissaoRepository,
            AtividadeService atividadeService,
            TurmaEstudanteService turmaEstudanteService,
            AcompanhamentoSubmissaoService acompanhamentoService,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {
        this.submissaoRepository = submissaoRepository;
        this.atividadeService = atividadeService;
        this.turmaEstudanteService = turmaEstudanteService;
        this.acompanhamentoService = acompanhamentoService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public SubmissaoResponseDTO executar(Long estudanteId, Long atividadeId, String nomeArquivo, String codigoFonte) {
        validarCodigoJava(nomeArquivo, codigoFonte);

        Atividade atividade = atividadeService.buscarEntidadePorId(atividadeId);

        if (!atividade.aceitaSubmissoes(OffsetDateTime.now(clock))) {
            throw new ConflitoException("Esta atividade está encerrada e não aceita novas submissões.");
        }

        if (!estudanteEstaEmTurma(atividade.getTurma().getCodigo(), estudanteId)) {
            throw new AcessoNegadoException("Estudante não pertence à turma desta atividade.");
        }

        int totalAnterior = submissaoRepository.countByEstudanteIdAndAtividadeId(estudanteId, atividadeId);
        int novaVersao = totalAnterior + 1;

        Submissao novaSubmissao = new Submissao(estudanteId, atividadeId, novaVersao, codigoFonte);
        Submissao salva = submissaoRepository.save(novaSubmissao);

        acompanhamentoService.registrarEstadoInicial(salva);
        eventPublisher.publishEvent(new SubmissaoRecebidaEvent(salva.getId()));
        return new SubmissaoResponseDTO(
                salva.getId(),
                salva.getVersao(),
                salva.getStatus().name(),
                salva.getDataHoraSubmissao(),
                "Solução recebida com sucesso! Versão " + salva.getVersao() + " registrada.");
    }

    private void validarCodigoJava(String nomeArquivo, String codigoFonte) {
        if (nomeArquivo == null || !nomeArquivo.endsWith(".java")) {
            throw new IllegalArgumentException("A solução deve ser submetida em um arquivo Java (.java).");
        }

        if (codigoFonte == null || codigoFonte.isBlank()) {
            throw new IllegalArgumentException("O código-fonte não pode estar vazio.");
        }
    }

    private boolean estudanteEstaEmTurma(String codigoTurma, Long estudanteId) {
        var estudantesDaTurma = turmaEstudanteService.listar(codigoTurma);

        return estudantesDaTurma.stream()
                .anyMatch(estudante -> estudante.getId().equals(estudanteId));
    }
}
