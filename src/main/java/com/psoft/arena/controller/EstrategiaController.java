package com.psoft.arena.controller;

import com.psoft.arena.dto.EstrategiaRequestDTO;
import com.psoft.arena.dto.EstrategiaResponseDTO;
import com.psoft.arena.service.EstrategiaService;
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

@Tag(name = "Estratégias de avaliação")
@RestController
@RequestMapping("/estrategias")
public class EstrategiaController {

    private final EstrategiaService service;

    public EstrategiaController(EstrategiaService service) {
        this.service = service;
    }


    @Operation(summary = "Cadastrar estratégia de avaliação", responses = {
    @ApiResponse(responseCode = "201", description = "Estratégia cadastrada.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "Dados da estratégia inválidos.", content = @Content)})
    @PostMapping
    public ResponseEntity<EstrategiaResponseDTO> cadastrar(@Valid @RequestBody EstrategiaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new EstrategiaResponseDTO(service.cadastrar(dto)));
    }

    @Operation(summary = "Listar estratégias de avaliação", responses = {
    @ApiResponse(responseCode = "200", description = "Lista de estratégias retornada.", useReturnTypeSchema = true)})
    @GetMapping
    public ResponseEntity<List<EstrategiaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(EstrategiaResponseDTO::new).toList());
    }

    @Operation(summary = "Consultar estratégia por ID", responses = {
    @ApiResponse(responseCode = "200", description = "Estratégia encontrada.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "O ID deve ser numérico.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Estratégia não encontrada.", content = @Content)})
    @GetMapping("/{id}")
    public ResponseEntity<EstrategiaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(new EstrategiaResponseDTO(service.buscarPorId(id)));
    }

    @Operation(summary = "Editar estratégia de avaliação", responses = {
    @ApiResponse(responseCode = "200", description = "Estratégia atualizada.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "ID ou dados da estratégia inválidos.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Estratégia não encontrada.", content = @Content)})
    @PutMapping("/{id}")
    public ResponseEntity<EstrategiaResponseDTO> editar(@PathVariable Long id,
                                                        @Valid @RequestBody EstrategiaRequestDTO dto) {
        return ResponseEntity.ok(new EstrategiaResponseDTO(service.editar(id, dto)));
    }
}
