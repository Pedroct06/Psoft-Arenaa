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

import java.util.List;

@RestController
@RequestMapping("/turmas/{codigo}/estudantes")
public class TurmaEstudanteController {

    private final TurmaEstudanteService service;

    public TurmaEstudanteController(TurmaEstudanteService service) {
        this.service = service;
    }

    @PostMapping("/{matricula}")
    public ResponseEntity<EstudanteResponseDTO> adicionar(@PathVariable String codigo,
                                                          @PathVariable String matricula) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EstudanteResponseDTO(service.adicionar(codigo, matricula)));
    }

    @DeleteMapping("/{matricula}")
    public ResponseEntity<Void> remover(@PathVariable String codigo, @PathVariable String matricula) {
        service.remover(codigo, matricula);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<EstudanteResponseDTO>> listar(@PathVariable String codigo) {
        return ResponseEntity.ok(service.listar(codigo).stream().map(EstudanteResponseDTO::new).toList());
    }
}