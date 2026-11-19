package com.psoft.arena.dto;

import java.time.OffsetDateTime;

import com.psoft.arena.model.Atividade;
import com.psoft.arena.model.StatusAtividade;
import com.psoft.arena.model.TipoEstrategia;

public record AtividadeEstudanteResponseDTO (
        Long id, 
        String titulo, 
        String codigoTurma,
        String nomeTurma,
        Long problemaId,
        String tituloProblema,
        String descricao,
        String descricaoProblema,
        String requisitosFuncionais,
        String requisitosProjeto,
        String estrategiaAvaliacao,
        TipoEstrategia tipoEstrategia,
        OffsetDateTime prazoSubmissao,
        StatusAtividade status,
        boolean aceitaSubmissoes) {

    public AtividadeEstudanteResponseDTO(Atividade a, OffsetDateTime agora) {
        this(a.getId(), a.getTitulo(), a.getTurma().getCodigo(), a.getTurma().getNome(),
                a.getProblema().getId(), a.getProblema().getTitulo(),
                a.getDescricao(), a.getProblema().getDescricao(),
                a.getProblema().getRequisitosFuncionais(), a.getProblema().getRequisitosProjeto(),
                a.getEstrategia().getNome(), a.getEstrategia().getTipo(),
                a.getPrazoSubmissao(), a.getStatus(), a.aceitaSubmissoes(agora));
    }

}
