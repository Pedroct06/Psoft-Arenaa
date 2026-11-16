package com.psoft.arena.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record AtividadeRequestDTO(
        @NotBlank @Size(max = 255) String titulo,
        @NotBlank @Size(max = 4000) String descricao,
        @NotNull OffsetDateTime prazoSubmissao,
        @NotBlank String codigoTurma,
        @NotNull @Positive Long problemaId,
        @NotNull @Positive Long estrategiaId,
        @NotNull @Valid ConfiguracaoLlmRequestDTO llm) { }
