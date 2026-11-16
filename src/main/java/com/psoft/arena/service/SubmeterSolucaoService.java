package com.psoft.arena.service;

import com.psoft.arena.dto.AtividadeResponseDTO;
import com.psoft.arena.dto.SubmissaoResponseDTO;
import com.psoft.arena.model.StatusAtividade;
import com.psoft.arena.model.Submissao;
import com.psoft.arena.repository.SubmissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class SubmeterSolucaoService {

  private final SubmissaoRepository submissaoRepository;
  private final AtividadeService atividadeService;
  private final TurmaEstudanteService turmaEstudanteService;

  public SubmeterSolucaoService(
      SubmissaoRepository submissaoRepository,
      AtividadeService atividadeService,
      TurmaEstudanteService turmaEstudanteService) {
    this.submissaoRepository = submissaoRepository;
    this.atividadeService = atividadeService;
    this.turmaEstudanteService = turmaEstudanteService;
  }

  @Transactional
  public SubmissaoResponseDTO executar(Long estudanteId, Long atividadeId, String nomeArquivo, String codigoFonte) {
    validarCodigoJava(nomeArquivo, codigoFonte);

    AtividadeResponseDTO atividade = atividadeService.buscarPorId(atividadeId);

    if (atividade.status() == StatusAtividade.CONCLUIDA || OffsetDateTime.now().isAfter(atividade.prazoSubmissao())) {
      throw new IllegalStateException("Esta atividade está encerrada e não aceita novas submissões.");
    }

    if (!estudanteEstaEmTurma(atividade, estudanteId)) {
      throw new SecurityException("Estudante não pertence à turma desta atividade.");
    }

    int totalAnterior = submissaoRepository.countByEstudanteIdAndAtividadeId(estudanteId, atividadeId);
    int novaVersao = totalAnterior + 1;

    Submissao novaSubmissao = new Submissao(estudanteId, atividadeId, novaVersao, codigoFonte);
    Submissao salva = submissaoRepository.save(novaSubmissao);

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

  private boolean estudanteEstaEmTurma(AtividadeResponseDTO atividade, Long estudanteId) {
    var estudantesDaTurma = turmaEstudanteService.listar(atividade.codigoTurma());

    return estudantesDaTurma.stream()
        .anyMatch(estudante -> estudante.getId().equals(estudanteId));
  }
}
