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

import java.util.List;

@RestController
@RequestMapping("/estrategias")
public class EstrategiaController {

    private final EstrategiaService service;

    public EstrategiaController(EstrategiaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EstrategiaResponseDTO> cadastrar(@Valid @RequestBody EstrategiaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new EstrategiaResponseDTO(service.cadastrar(dto)));
    }

    @GetMapping
    public ResponseEntity<List<EstrategiaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(EstrategiaResponseDTO::new).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstrategiaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(new EstrategiaResponseDTO(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstrategiaResponseDTO> editar(@PathVariable Long id,
                                                        @Valid @RequestBody EstrategiaRequestDTO dto) {
        return ResponseEntity.ok(new EstrategiaResponseDTO(service.editar(id, dto)));
    }
}
