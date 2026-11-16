package com.psoft.arena.controller;

import com.psoft.arena.dto.*;
import com.psoft.arena.service.AtividadeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/atividades")
@Tag(name = "Atividades", description = "Criação, consulta e encerramento de atividades (US6)")
public class AtividadeController {
    private final AtividadeService service;
    public AtividadeController(AtividadeService service) { this.service = service; }
    @PostMapping
    @Operation(summary = "Criar atividade aberta", description = "Requer turma ativa, problema ativo e estratégia existente. A chave do LLM nunca é devolvida.")
    public ResponseEntity<AtividadeResponseDTO> cadastrar(@Valid @RequestBody AtividadeRequestDTO dto) {
        AtividadeResponseDTO resposta = service.cadastrar(dto);
        return ResponseEntity.created(URI.create("/atividades/" + resposta.id())).body(resposta);
    }
    @GetMapping
    @Operation(summary = "Listar atividades")
    public List<AtividadeResponseDTO> listar() { return service.listar(); }
    @GetMapping("/{id}")
    @Operation(summary = "Consultar atividade")
    public AtividadeResponseDTO buscar(@PathVariable Long id) { return service.buscarPorId(id); }
    @PatchMapping("/{id}/encerrar")
    @Operation(summary = "Concluir atividade", description = "Atividades concluídas deixam de aceitar novas submissões.")
    public AtividadeResponseDTO encerrar(@PathVariable Long id) { return service.encerrar(id); }
}
