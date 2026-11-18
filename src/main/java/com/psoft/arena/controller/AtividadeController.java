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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@Tag(name = "Atividades", description = "Criação, consulta e encerramento de atividades (US6)")
@RestController
@RequestMapping("/atividades")
public class AtividadeController {
    private final AtividadeService service;

    public AtividadeController(AtividadeService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Criar atividade aberta", description = "Requer turma ativa, problema ativo e estratégia existente. A chave do LLM nunca é devolvida.",
        responses = {
        @ApiResponse(responseCode = "201", description = "Atividade criada.", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Dados da atividade ou URL da API inválidos.", content = @Content),
        @ApiResponse(responseCode = "404", description = "Turma, problema ou estratégia não encontrada.", content = @Content),
        @ApiResponse(responseCode = "409", description = "A turma está encerrada ou o problema está inativo.", content = @Content)})
    public ResponseEntity<AtividadeResponseDTO> cadastrar(@Valid @RequestBody AtividadeRequestDTO dto) {
        AtividadeResponseDTO resposta = service.cadastrar(dto);
        return ResponseEntity.created(URI.create("/atividades/" + resposta.id())).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar atividades", responses = {
        @ApiResponse(responseCode = "200", description = "Lista de atividades retornada.", useReturnTypeSchema = true)})
    public List<AtividadeResponseDTO> listar() { return service.listar(); }
    
    @GetMapping("/{id}")
    @Operation(summary = "Consultar atividade", responses = {
        @ApiResponse(responseCode = "200", description = "Atividade encontrada.", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "Atividade não encontrada.", content = @Content),
        @ApiResponse (responseCode = "400", description = "O ID deve ser numérico.", content = @Content)})
    public AtividadeResponseDTO buscar(@PathVariable Long id) { return service.buscarPorId(id); }

    @Operation(
    summary = "Encerrar atividade",
    description = "Altera o status para CONCLUIDA. A atividade deixa de aceitar submissões.",
    responses = {
        @ApiResponse(responseCode = "200", description = "Atividade concluída.", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "O ID da atividade deve ser numérico.", content = @Content),
        @ApiResponse(responseCode = "404", description = "Atividade não encontrada.", content = @Content)})
    @PatchMapping("/{id}/encerrar")
    public AtividadeResponseDTO encerrar(@PathVariable Long id) { return service.encerrar(id); }
}
