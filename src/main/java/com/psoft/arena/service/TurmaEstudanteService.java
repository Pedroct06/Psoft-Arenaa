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

    // Somente turmas ativas recebem novas associações (a verificação fica em TurmaService).
    @Transactional
    public Estudante adicionar(String codigoTurma, String matricula) {
        Turma turma = turmaService.garantirTurmaAtiva(codigoTurma);
        Estudante estudante = estudanteService.buscarPorMatricula(matricula);
        if (turma.possuiEstudante(estudante)) {
            throw new ConflitoException("O