package com.psoft.arena.controller;

import com.psoft.arena.dto.TurmaRequestDTO;
import com.psoft.arena.dto.TurmaResponseDTO;
import com.psoft.arena.dto.TurmaUpdateDTO;
import com.psoft.arena.service.TurmaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Turmas")
@RestController
@RequestMapping("/turmas")
public class TurmaController {

    private final TurmaService service;

    public TurmaController(TurmaService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar turma", responses = {
    @ApiResponse(responseCode = "201", description = "Turma cadastrada.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "Dados da turma inválidos.", content = @Content),
    @ApiResponse(responseCode = "409", description = "Turma já cadastrada.", content = @Content)})
    @PostMapping
    public ResponseEntity<TurmaResponseDTO> cadastrar(@Valid @RequestBody TurmaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new TurmaResponseDTO(service.cadastrar(dto)));
    }

    @Operation(summary = "Listar turmas", responses = {
    @ApiResponse(responseCode = "200", description = "Lista de turmas retornada.", useReturnTypeSchema = true)})
    @GetMapping
    public ResponseEntity<List<TurmaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(TurmaResponseDTO::new).toList());
    }

    @Operation(summary = "Consultar turma por código", responses = {
    @ApiResponse(responseCode = "200", description = "Turma encontrada.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "404", description = "Turma não encontrada.", content = @Content)}) 
    @GetMapping("/{codigo}")
    public ResponseEntity<TurmaResponseDTO> buscar(@PathVariable String codigo) {
        return ResponseEntity.ok(new TurmaResponseDTO(service.buscarPorCodigo(codigo)));
    }

    @Operation(summary = "Editar turma", responses = {
    @ApiResponse(responseCode = "200", description = "Turma atualizada.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "Dados da turma inválidos.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Turma não encontrada.", content = @Content)})
    @PutMapping("/{codigo}")
    public ResponseEntity<TurmaResponseDTO> editar(@PathVariable String codigo,
                                                   @Valid @RequestBody TurmaUpdateDTO dto) {
        return ResponseEntity.ok(new TurmaResponseDTO(service.editar(codigo, dto)));
    }

    @Operation(summary = "Encerrar turma", responses = {
    @ApiResponse(responseCode = "200", description = "Turma encerrada.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "404", description = "Turma não encontrada.", content = @Content)})
    @PatchMapping("/{codigo}/encerrar")
    public ResponseEntity<TurmaResponseDTO> encerrar(@PathVariable String codigo) {
        return ResponseEntity.ok(new TurmaResponseDTO(service.encerrar(codigo)));
    }
}
