package com.psoft.arena.dto;

import java.time.LocalDateTime;

public record SubmissaoResponseDTO(
                Long submissaoId,
                int versao,
                String status,
                LocalDateTime dataHora,
                String mensagem) {
}
