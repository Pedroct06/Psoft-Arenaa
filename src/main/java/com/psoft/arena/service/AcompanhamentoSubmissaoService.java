package com.psoft.arena.service;

import com.psoft.arena.dto.AcompanhamentoSubmissaoResponseDTO;
import com.psoft.arena.model.HistoricoSubmissao;
import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.model.Submissao;
import com.psoft.arena.repository.HistoricoSubmissaoRepository;
import com.psoft.arena.repository.SubmissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AcompanhamentoSubmissaoService {

    private final SubmissaoRepository submissaoRepository;
    private final HistoricoSubmissaoRepository historicoRepository;

    public AcompanhamentoSubmissaoService(
            SubmissaoRepository submissaoRepository,
            HistoricoSubmissaoRepository historicoRepository) {
        this.submissaoRepository = submissaoRepository;
        this.historicoRepository = historicoRepository;
    }

    @Transactional
    public void registrarEstadoInicial(Submissao submissao) {
        historicoRepository.save(new HistoricoSubmissao(
                submissao.getId(),
                null,
                submissao.getStatus()
        ));
        System.out.println(
        "Submissão " + submissao.getId()
        + " | Estudante " + submissao.getEstudanteId()
        + " | Status inicial: " + submissao.getStatus()
        );
    }

    @Transactional
    public void alterarStatus(Long submissaoId, StatusSubmissao novoStatus) {
        Submissao submissao = submissaoRepository.findById(submissaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Submissão não encontrada."));

        StatusSubmissao statusAnterior = submissao.getStatus();

        submissao.alterarStatus(novoStatus);

        submissaoRepository.save(submissao);

        historicoRepository.save(new HistoricoSubmissao(
                submissaoId,
                statusAnterior,
                novoStatus
        ));

        System.out.println(
                "Submissão " + submissaoId
                + " | Estudante " + submissao.getEstudanteId()
                + " | Status: " + statusAnterior
                + " -> " + novoStatus
        );
    }

    @Transactional(readOnly = true)
    public List<HistoricoSubmissao> consultarHistorico(Long submissaoId) {
        return historicoRepository.findBySubmissaoIdOrderByDataHoraAsc(submissaoId);
    }
    @Transactional(readOnly = true)
    public AcompanhamentoSubmissaoResponseDTO consultarAcompanhamento(
        Long submissaoId, Long estudanteId, Long atividadeId) {

    Submissao submissao = submissaoRepository
            .findByIdAndEstudanteIdAndAtividadeId(
                    submissaoId, estudanteId, atividadeId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Submissão não encontrada para este estudante e atividade."));

    List<AcompanhamentoSubmissaoResponseDTO.HistoricoStatusResponseDTO> historico =
            consultarHistorico(submissaoId).stream()
                    .map(item -> new AcompanhamentoSubmissaoResponseDTO.HistoricoStatusResponseDTO(
                            item.getStatusAnterior() == null ? null : item.getStatusAnterior().name(),
                            item.getStatusNovo().name(),
                            item.getDataHora()))
                    .toList();

    return new AcompanhamentoSubmissaoResponseDTO(
            submissao.getId(),
            submissao.getStatus().name(),
            historico);
    }
}
