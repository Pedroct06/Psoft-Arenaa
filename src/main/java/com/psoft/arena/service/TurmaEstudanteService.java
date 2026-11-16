package com.psoft.arena.service;

import com.psoft.arena.exception.ConflitoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Estudante;
import com.psoft.arena.model.Turma;
import com.psoft.arena.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TurmaEstudanteService {

    private final TurmaService turmaService;
    private final EstudanteService estudanteService;
    private final TurmaRepository turmaRepository;

    public TurmaEstudanteService(TurmaService turmaService, EstudanteService estudanteService,
                                 TurmaRepository turmaRepository) {
        this.turmaService = turmaService;
        this.estudanteService = estudanteService;
        this.turmaRepository = turmaRepository;
    }

    @Transactional
    public Estudante adicionar(String codigoTurma, String matricula) {
        Turma turma = turmaService.garantirTurmaAtiva(codigoTurma);
        Estudante estudante = estudanteService.buscarPorMatricula(matricula);
        if (turma.possuiEstudante(estudante)) {
            throw new ConflitoException("O estudante já está associado a esta turma.");
        }
        turma.adicionarEstudante(estudante);
        turmaRepository.save(turma);
        return estudante;
    }

    @Transactional
    public void remover(String codigoTurma, String matricula) {
        Turma turma = turmaService.buscarPorCodigo(codigoTurma);
        Estudante estudante = estudanteService.buscarPorMatricula(matricula);
        if (!turma.possuiEstudante(estudante)) {
            throw new RecursoNaoEncontradoException("Estudante não associado à turma.");
        }
        turma.removerEstudante(estudante);
        turmaRepository.save(turma);
    }

    @Transactional(readOnly = true)
    public List<Estudante> listar(String codigoTurma) {
        return List.copyOf(turmaService.buscarPorCodigo(codigoTurma).getEstudantes());
    }
}
