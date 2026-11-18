package com.psoft.arena.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import com.psoft.arena.dto.AtividadeEstudanteResponseDTO;
import com.psoft.arena.exception.AcessoNegadoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Atividade;
import com.psoft.arena.model.Estudante;
import com.psoft.arena.repository.AtividadeRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AtividadeConsultaService {
    
    private final AtividadeRepository atividadeRepository;
    private final EstudanteService estudanteService;
    private final Clock clock;

    public AtividadeConsultaService(AtividadeRepository atividadeRepository, EstudanteService estudanteService, Clock clock){
        this.atividadeRepository = atividadeRepository;
        this.estudanteService = estudanteService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AtividadeEstudanteResponseDTO> listar(String matricula) {
        Estudante estudante = estudanteService.buscarPorMatricula(matricula);
        OffsetDateTime agora = OffsetDateTime.now(clock);
        return atividadeRepository
                .findByTurmaEstudantesMatriculaOrderByPrazoSubmissaoAsc(estudante.getMatricula())
                .stream().map(a -> new AtividadeEstudanteResponseDTO(a, agora)).toList();
    }

    @Transactional(readOnly = true)
    public AtividadeEstudanteResponseDTO buscar(String matricula, Long atividadeId) {
        Estudante estudante = estudanteService.buscarPorMatricula(matricula);
        Atividade atividade = atividadeRepository.findById(atividadeId).orElseThrow(() ->
                new RecursoNaoEncontradoException("Atividade com ID " + atividadeId + " não encontrada."));
        if (!atividade.getTurma().possuiEstudante(estudante)) {
            throw new AcessoNegadoException("O estudante não participa da turma desta atividade.");
        }
        return new AtividadeEstudanteResponseDTO(atividade, OffsetDateTime.now(clock));
    }
}
