package com.psoft.arena.service;

import com.psoft.arena.dto.*;
import com.psoft.arena.model.*;
import com.psoft.arena.repository.AtividadeRepository;
import com.psoft.arena.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.net.URI;
import java.util.List;

@Service
public class AtividadeService {
    private final AtividadeRepository repository;
    private final TurmaService turmaService;
    private final ProblemaService problemaService;
    private final EstrategiaService estrategiaService;
    private final CriptografiaService criptografia;
    public AtividadeService(AtividadeRepository repository, TurmaService turmaService,
            ProblemaService problemaService, EstrategiaService estrategiaService, CriptografiaService criptografia) {
        this.repository = repository; this.turmaService = turmaService;
        this.problemaService = problemaService; this.estrategiaService = estrategiaService;
        this.criptografia = criptografia;
    }
    @Transactional
    public AtividadeResponseDTO cadastrar(AtividadeRequestDTO dto) {
        validarUrl(dto.llm().urlApi());
        Turma turma = turmaService.garantirTurmaAtiva(dto.codigoTurma());
        Estrategia estrategia = estrategiaService.buscarPorId(dto.estrategiaId());
        Problema problema = problemaService.utilizar(dto.problemaId());
        ConfiguracaoLlm llm = new ConfiguracaoLlm(dto.llm().nomeModelo().trim(),
                dto.llm().urlApi().trim(), criptografia.criptografar(dto.llm().chaveApi()));
        Atividade atividade = new Atividade(dto.titulo().trim(), dto.descricao().trim(),
                dto.prazoSubmissao(), turma, problema, estrategia, llm);
        return new AtividadeResponseDTO(repository.save(atividade));
    }
    private void validarUrl(String texto) {
        try {
            URI uri = URI.create(texto.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getFragment() != null || uri.getQuery() != null)
                throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "URL da API deve ser HTTPS, com host e sem credenciais, query ou fragmento.");
        }
    }
    private Atividade carregar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Atividade com ID " + id + " não encontrada."));
    }
    @Transactional(readOnly = true)
    public AtividadeResponseDTO buscarPorId(Long id) { return new AtividadeResponseDTO(carregar(id)); }
    @Transactional(readOnly = true)
    public List<AtividadeResponseDTO> listar() {
        return repository.findAll().stream().map(AtividadeResponseDTO::new).toList();
    }
    @Transactional
    public AtividadeResponseDTO encerrar(Long id) {
        Atividade atividade = carregar(id); atividade.encerrar();
        return new AtividadeResponseDTO(repository.save(atividade));
    }
    // Chamar dentro da transação que salva a submissão, quando essa US existir.
    @Transactional(readOnly = true)
    public Atividade garantirAtividadeAberta(Long id) {
        Atividade atividade = carregar(id);
        if (!atividade.isAberta()) throw new ConflitoException("Atividade concluída não aceita novas submissões.");
        return atividade;
    }
}
