package com.psoft.arena.service;

import com.psoft.arena.dto.ProblemaRequestDTO;
import com.psoft.arena.exception.ConflitoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Problema;
import com.psoft.arena.repository.ProblemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProblemaService {

    private final ProblemaRepository repository;

    public ProblemaService(ProblemaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Problema cadastrar(ProblemaRequestDTO dto) {
        return repository.save(new Problema(dto.getTitulo().trim(), dto.getDescricao().trim(),
                dto.getRequisitosFuncionais().trim(), dto.getRequisitosProjeto().trim()));
    }

    @Transactional(readOnly = true)
    public Problema buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Problema com ID " + id + " não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Problema> listar() {
        return repository.findAll();
    }

    @Transactional
    public Problema editar(Long id, ProblemaRequestDTO dto) {
        Problema problema = buscarPorId(id);
        problema.editar(dto.getTitulo().trim(), dto.getDescricao().trim(),
                dto.getRequisitosFuncionais().trim(), dto.getRequisitosProjeto().trim());
        return repository.save(problema);
    }

    @Transactional
    public Problema desativar(Long id) {
        Problema problema = buscarPorId(id);
        problema.desativar();
        return repository.save(problema);
    }

    // Ponto de integração para a futura US de Atividades: chamar ao associar um problema a uma atividade.
    @Transactional
    public Problema utilizar(Long id) {
        Problema problema = buscarPorId(id);
        if (!problema.isAtivo()) {
            throw new ConflitoException("Problema com ID " + id
                    + " está inativo e não pode ser associado a novas atividades.");
        }
        problema.marcarComoUtilizado();
        return repository.save(problema);
    }

    @Transactional
    public void remover(Long id) {
        Problema problema = buscarPorId(id);
        if (problema.isUtilizado()) {
            throw new ConflitoException("Problema com ID " + id + " não pode ser removido, pois já foi utilizado.");
        }
        repository.delete(problema);
    }
}
