package com.psoft.arena.service;

import com.psoft.arena.dto.EstrategiaRequestDTO;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Estrategia;
import com.psoft.arena.repository.EstrategiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstrategiaService {

    private final EstrategiaRepository repository;

    public EstrategiaService(EstrategiaRepository repository) {
        this.repository = repository;
    }

    // Podem existir várias estratégias do mesmo tipo (com prompts diferentes), então não há checagem de duplicidade.
    @Transactional
    public Estrategia cadastrar(EstrategiaRequestDTO dto) {
        return repository.save(new Estrategia(dto.getNome().trim(), dto.getTipo(),
                dto.getDescricao().trim(), dto.getPromptAvaliacao().trim()));
    }

    @Transactional(readOnly = true)
    public Estrategia buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estratégia com ID " + id + " não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<Estrategia> listar() {
        return repository.findAll();
    }

    @Transactional
    public Estrategia editar(Long id, EstrategiaRequestDTO dto) {
        Estrategia estrategia = buscarPorId(id);
        estrategia.editar(dto.getNome().trim(), dto.getTipo(),
                dto.getDescricao().trim(), dto.getPromptAvaliacao().trim());
        return repository.save(estrategia);
    }
}
