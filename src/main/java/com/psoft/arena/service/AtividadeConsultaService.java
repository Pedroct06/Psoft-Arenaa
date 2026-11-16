package com.psoft.arena.service;

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

    public AtividadeConsultaService(AtividadeRepository atividadeRepository, EstudanteService estudanteService){
        this.atividadeRepository = atividadeRepository;
        this.estudanteService = estudanteService;
    }

    @Transactional(readOnly = true)
    public List<AtividadeEstudanteResponseDTO> listar(String matricula) {
        Estudante estudante = estudanteService.buscarPorMatricula(matricula);
        return atividadeRepository
                .findByTurmaEstudantesMatriculaOrderByPrazoSubmissaoAsc(estudante.getMatricula())
                .stream().map(AtividadeEstudanteResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public AtividadeEstudanteResponseDTO buscar(String matricula, Long atividadeId) {
        Estudante estudante = estudanteService.buscarPorMatricula(matricula);
        Atividade atividade = atividadeRepository.findById(atividadeId).orElseThrow(() ->
                new RecursoNaoEncontradoException("Atividade com ID " + atividadeId + " não encontrada."));
        if (!atividade.getTurma().possuiEstudante(estudante)) {
            throw new AcessoNegadoException("O estudante não participa da turma desta atividade.");
        }
        return new AtividadeEstudanteResponseDTO(atividade);
    }
}
