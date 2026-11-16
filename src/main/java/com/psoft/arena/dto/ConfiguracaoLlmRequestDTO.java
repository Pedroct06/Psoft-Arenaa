package com.psoft.arena.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ConfiguracaoLlmRequestDTO(
        @NotBlank @Size(max = 255) String nomeModelo,
        @NotBlank @Size(max = 2048) String urlApi,
        @NotBlank @Size(max = 2048)
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String chaveApi) {
    @Override public String toString() { return "ConfiguracaoLlmRequestDTO[chaveApi=OCULTA]"; }
}
