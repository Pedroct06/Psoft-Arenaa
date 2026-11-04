package com.psoft.arena.service;

import com.psoft.arena.dto.TurmaRequestDTO;
import com.psoft.arena.dto.TurmaUpdateDTO;
import com.psoft.arena.exception.ConflitoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Turma;
import com.psoft.arena.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TurmaService {

    private final TurmaRepository repository;

    public TurmaService(TurmaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Turma cadastrar(TurmaRequestDTO dto) {
        String codigo = dto.getCodigo().trim();
        if (repository.existsByCodigo(codigo)) {
            throw new ConflitoException("Já existe uma turma com o código " + codigo + ".");
        }
        return repository.save(new Turma(dto.getNome().trim(), codigo,
                dto.getPeriodoLetivo().trim(), dto.getProfessorResponsavel().trim()));
    }

    // Turmas encerradas continuam disponíveis para consulta (histórico).
    @Transactional(readOnly = true)
    public Turma buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo.trim())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma com código " + codigo + " não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<Turma> listar() {
        return repository.findAll();
    }

    @Transactional
    public Turma editar(String codigo, TurmaUpdateDTO dto) {
        Turma turma = buscarPorCodigo(codigo);
        turma.editar(dto.getNome().trim(), dto.getPeriodoLetivo().trim(), dto.getProfessorResponsavel().trim());
        return repository.save(turma);
    }

    @Transactional
    public Turma encerrar(String codigo) {
        Turma turma = buscarPorCodigo(codigo);
        turma.encerrar();
        return repository.save(turma);
    }

    // Ponto de integração para as futuras USs: chamar antes de associar estudantes ou criar atividades na turma.
    @Transactional(readOnly = true)
    public Turma garantirTurmaAtiva(String codigo) {
        Turma turma = buscarPorCodigo(codigo);
        if (!turma.isAtiva()) {
            throw new ConflitoException("Turma " + turma.getCodigo()
                    + " está encerrada e não pode receber novos estudantes ou novas atividades.");
        }
        return turma;
    }
}
