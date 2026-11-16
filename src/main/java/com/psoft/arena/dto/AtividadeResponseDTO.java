package com.psoft.arena.dto;

import com.psoft.arena.model.*;
import java.time.OffsetDateTime;

public record AtividadeResponseDTO(Long id, String titulo, String descricao,
        OffsetDateTime prazoSubmissao, StatusAtividade status, String codigoTurma,
        Long problemaId, Long estrategiaId, String nomeModelo, String urlApi) {
    public AtividadeResponseDTO(Atividade a) {
        this(a.getId(), a.getTitulo(), a.getDescricao(), a.getPrazoSubmissao(), a.getStatus(),
                a.getTurma().getCodigo(), a.getProblema().getId(), a.getEstrategia().getId(),
                a.getConfiguracaoLlm().getNomeModelo(), a.getConfiguracaoLlm().getUrlApi());
    }
}
