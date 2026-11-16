package com.psoft.arena.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AcompanhamentoSubmissaoResponseDTO(
        Long submissaoId,
        String statusAtual,
        List<HistoricoStatusResponseDTO> historico) {

    public record HistoricoStatusResponseDTO(
            String statusAnterior,
            String statusNovo,
            LocalDateTime dataHora) {
    }
}
