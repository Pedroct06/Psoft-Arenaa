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

import java.util.List;

@RestController
@RequestMapping("/turmas")
public class TurmaController {

    private final TurmaService service;

    public TurmaController(TurmaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TurmaResponseDTO> cadastrar(@Valid @RequestBody TurmaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new TurmaResponseDTO(service.cadastrar(dto)));
    }

    @GetMapping
    public ResponseEntity<List<TurmaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(TurmaResponseDTO::new).toList());
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<TurmaResponseDTO> buscar(@PathVariable String codigo) {
        return ResponseEntity.ok(new TurmaResponseDTO(service.buscarPorCodigo(codigo)));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<TurmaResponseDTO> editar(@PathVariable String codigo,
                                                   @Valid @RequestBody TurmaUpdateDTO dto) {
        return ResponseEntity.ok(new TurmaResponseDTO(service.editar(codigo, dto)));
    }

    @PatchMapping("/{codigo}/encerrar")
    public ResponseEntity<TurmaResponseDTO> encerrar(@PathVariable String codigo) {
        return ResponseEntity.ok(new TurmaResponseDTO(service.encerrar(codigo)));
    }
}
