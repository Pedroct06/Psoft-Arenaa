package com.psoft.arena.service;

import com.psoft.arena.dto.EstudanteRequestDTO;
import com.psoft.arena.dto.EstudanteUpdateDTO;
import com.psoft.arena.exception.ConflitoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Estudante;
import com.psoft.arena.repository.EstudanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstudanteService {

    private final EstudanteRepository repository;

    public EstudanteService(EstudanteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Estudante cadastrar(EstudanteRequestDTO dto) {
        String matricula = dto.getMatricula().trim();
        if (repository.existsByMatricula(matricula)) {
            throw new ConflitoException("Já existe um estudante com a matrícula " + matricula + ".");
        }
        return repository.save(new Estudante(dto.getNome().trim(), matricula, dto.getEmail().trim()));
    }

    @Transactional(readOnly = true)
    public Estudante buscarPorMatricula(String matricula) {
        return repository.findByMatricula(matricula.trim())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Estudante com matrícula " + matricula + " não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Estudante> listar() {
        return repository.findAll();
    }

    @Transactional
    public Estudante editar(String matricula, EstudanteUpdateDTO dto) {
        Estudante estudante = buscarPorMatricula(matricula);
        estudante.setNome(dto.getNome().trim());
        estudante.setEmail(dto.getEmail().trim());
        return repository.save(estudante);
    }
}
