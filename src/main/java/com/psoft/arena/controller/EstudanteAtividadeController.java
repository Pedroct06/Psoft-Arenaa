package com.psoft.arena.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.psoft.arena.dto.AtividadeEstudanteResponseDTO;
import com.psoft.arena.service.AtividadeConsultaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController 
@RequestMapping("/estudantes/{matricula}/atividades")
@Tag(name = "consulta de atividades", description = "Atividades vistas pelo estudante")
public class EstudanteAtividadeController {
    private final AtividadeConsultaService service;

    public EstudanteAtividadeController(AtividadeConsultaService service){
        this.service = service;
    }

    @GetMapping
    @Operation (summary = "Listar atividades das turmas dos estudantes", description = "Inclui atividades concluidas, que continuam consultaveis, mas nao aceitam submissoes")
    public List<AtividadeEstudanteResponseDTO> listar(@PathVariable String matricula){
        return service.listar(matricula);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar uma atividade", 
                description = "403 se a atividade  pertence a uma turma da qual o estudante nao participa")
    public AtividadeEstudanteResponseDTO buscar(@PathVariable String matricula, @PathVariable Long id){
        return service.buscar((matricula), id);
    }
}
