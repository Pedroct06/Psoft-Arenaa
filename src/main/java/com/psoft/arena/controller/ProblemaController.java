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

import java.util.List;

@RestController
@RequestMapping("/problemas")
public class ProblemaController {

    private final ProblemaService service;

    public ProblemaController(ProblemaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProblemaResponseDTO> cadastrar(@Valid @RequestBody ProblemaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProblemaResponseDTO(service.cadastrar(dto)));
    }

    @GetMapping
    public ResponseEntity<List<ProblemaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(ProblemaResponseDTO::new).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(new ProblemaResponseDTO(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProblemaResponseDTO> editar(@PathVariable Long id,
                                                      @Valid @RequestBody ProblemaRequestDTO dto) {
        return ResponseEntity.ok(new ProblemaResponseDTO(service.editar(id, dto)));
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<ProblemaResponseDTO> desativar(@PathVariable Long id) {
        return ResponseEntity.ok(new ProblemaResponseDTO(service.desativar(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
