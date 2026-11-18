package com.psoft.arena.controller;

import com.psoft.arena.dto.EstudanteResponseDTO;
import com.psoft.arena.service.TurmaEstudanteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Turmas - Estudantes")
@RestController
@RequestMapping("/turmas/{codigo}/estudantes")
public class TurmaEstudanteController {

    private final TurmaEstudanteService service;

    public TurmaEstudanteController(TurmaEstudanteService service) {
        this.service = service;
    }

    @Operation(summary = "Adicionar estudante à turma", responses = {
    @ApiResponse(responseCode = "201", description = "Estudante adicionado à turma.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "Dados inválidos.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Turma ou estudante não encontrado.", content = @Content)})
    @PostMapping("/{matricula}")
    public ResponseEntity<EstudanteResponseDTO> adicionar(@PathVariable String codigo,
                                                          @PathVariable String matricula) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EstudanteResponseDTO(service.adicionar(codigo, matricula)));
    }

    @Operation(summary = "Remover estudante da turma", responses = {
    @ApiResponse(responseCode = "204", description = "Estudante removido da turma.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "Dados inválidos.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Turma ou estudante não encontrado.", content = @Content)})
    @DeleteMapping("/{matricula}")
    public ResponseEntity<Void> remover(@PathVariable String codigo, @PathVariable String matricula) {
        service.remover(codigo, matricula);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar estudantes da turma", responses = {
    @ApiResponse(responseCode = "200", description = "Lista de estudantes retornada.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "404", description = "Turma não encontrada.", content = @Content)})
    @GetMapping
    public ResponseEntity<List<EstudanteResponseDTO>> listar(@PathVariable String codigo) {
        return ResponseEntity.ok(service.listar(codigo).stream().map(EstudanteResponseDTO::new).toList());
    }
}