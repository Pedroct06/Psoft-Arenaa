package com.psoft.arena.controller;

import com.psoft.arena.dto.ProblemaRequestDTO;
import com.psoft.arena.dto.ProblemaResponseDTO;
import com.psoft.arena.service.ProblemaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

@Tag(name = "Problemas")
@RestController
@RequestMapping("/problemas")
public class ProblemaController {

    private final ProblemaService service;

    public ProblemaController(ProblemaService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar problema", responses = {
    @ApiResponse(responseCode = "201", description = "Problema cadastrado.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "Dados do problema inválidos.", content = @Content)})
    @PostMapping
    public ResponseEntity<ProblemaResponseDTO> cadastrar(@Valid @RequestBody ProblemaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProblemaResponseDTO(service.cadastrar(dto)));
    }

    @Operation(summary = "Listar problemas", responses = {
    @ApiResponse(responseCode = "200", description = "Lista de problemas retornada.", useReturnTypeSchema = true)})
    @GetMapping
    public ResponseEntity<List<ProblemaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(ProblemaResponseDTO::new).toList());
    }

    @Operation(summary = "Consultar problema por ID", responses = {
    @ApiResponse(responseCode = "200", description = "Problema encontrado.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "O ID deve ser numérico.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Problema não encontrado.", content = @Content)})
    @GetMapping("/{id}")
    public ResponseEntity<ProblemaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(new ProblemaResponseDTO(service.buscarPorId(id)));
    }

    @Operation(summary = "Editar problema", responses = {
    @ApiResponse(responseCode = "200", description = "Problema atualizado.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "ID ou dados do problema inválidos.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Problema não encontrado.", content = @Content)})
    @PutMapping("/{id}")
    public ResponseEntity<ProblemaResponseDTO> editar(@PathVariable Long id,
                                                      @Valid @RequestBody ProblemaRequestDTO dto) {
        return ResponseEntity.ok(new ProblemaResponseDTO(service.editar(id, dto)));
    }

    @Operation(summary = "Desativar problema", responses = {
    @ApiResponse(responseCode = "200", description = "Problema desativado.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "O ID deve ser numérico.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Problema não encontrado.", content = @Content)})
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<ProblemaResponseDTO> desativar(@PathVariable Long id) {
        return ResponseEntity.ok(new ProblemaResponseDTO(service.desativar(id)));
    }

    @Operation(summary = "Remover problema", responses = {
    @ApiResponse(responseCode = "204", description = "Problema removido.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "O ID deve ser numérico.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Problema não encontrado.", content = @Content),
    @ApiResponse(responseCode = "409", description = "Problema não pode ser removido, pois está associado a uma ou mais turmas.", content = @Content)})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
