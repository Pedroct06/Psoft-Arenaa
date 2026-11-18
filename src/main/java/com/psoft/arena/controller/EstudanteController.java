package com.psoft.arena.controller;

import com.psoft.arena.dto.EstudanteRequestDTO;
import com.psoft.arena.dto.EstudanteResponseDTO;
import com.psoft.arena.dto.EstudanteUpdateDTO;
import com.psoft.arena.service.EstudanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

@Tag (name = "Estudantes")
@RestController
@RequestMapping("/estudantes")
public class EstudanteController {

    private final EstudanteService service;

    public EstudanteController(EstudanteService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar estudante", responses = {
    @ApiResponse(responseCode = "201", description = "Estudante cadastrado.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "Dados do estudante inválidos.", content = @Content),
    @ApiResponse(responseCode = "409", description = "Estudante já cadastrado.", content = @Content)})
    @PostMapping
    public ResponseEntity<EstudanteResponseDTO> cadastrar(@Valid @RequestBody EstudanteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new EstudanteResponseDTO(service.cadastrar(dto)));
    }

    @Operation(summary = "Listar estudantes", responses = {
    @ApiResponse(responseCode = "200", description = "Lista de estudantes retornada.", useReturnTypeSchema = true)})
    @GetMapping
    public ResponseEntity<List<EstudanteResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(EstudanteResponseDTO::new).toList());
    }

    @Operation(summary = "Consultar estudante por matrícula", responses = {
    @ApiResponse(responseCode = "200", description = "Estudante encontrado.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "404", description = "Estudante não encontrado.", content = @Content)})
    @GetMapping("/{matricula}")
    public ResponseEntity<EstudanteResponseDTO> buscar(@PathVariable String matricula) {
        return ResponseEntity.ok(new EstudanteResponseDTO(service.buscarPorMatricula(matricula)));
    }

    @Operation(summary = "Editar estudante", responses = {
    @ApiResponse(responseCode = "200", description = "Estudante atualizado.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "Dados do estudante inválidos.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Estudante não encontrado.", content = @Content)})
    @PutMapping("/{matricula}")
    public ResponseEntity<EstudanteResponseDTO> editar(@PathVariable String matricula,
                                                       @Valid @RequestBody EstudanteUpdateDTO dto) {
        return ResponseEntity.ok(new EstudanteResponseDTO(service.editar(matricula, dto)));
    }
}
