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

import java.util.List;

@RestController
@RequestMapping("/estudantes")
public class EstudanteController {

    private final EstudanteService service;

    public EstudanteController(EstudanteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EstudanteResponseDTO> cadastrar(@Valid @RequestBody EstudanteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new EstudanteResponseDTO(service.cadastrar(dto)));
    }

    @GetMapping
    public ResponseEntity<List<EstudanteResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(EstudanteResponseDTO::new).toList());
    }

    @GetMapping("/{matricula}")
    public ResponseEntity<EstudanteResponseDTO> buscar(@PathVariable String matricula) {
        return ResponseEntity.ok(new EstudanteResponseDTO(service.buscarPorMatricula(matricula)));
    }

    @PutMapping("/{matricula}")
    public ResponseEntity<EstudanteResponseDTO> editar(@PathVariable String matricula,
                                                       @Valid @RequestBody EstudanteUpdateDTO dto) {
        return ResponseEntity.ok(new EstudanteResponseDTO(service.editar(matricula, dto)));
    }
}
