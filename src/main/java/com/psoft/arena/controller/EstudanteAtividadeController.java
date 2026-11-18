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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController 
@RequestMapping("/estudantes/{matricula}/atividades")
@Tag(name = "consulta de atividades", description = "Atividades vistas pelo estudante")
public class EstudanteAtividadeController {
    private final AtividadeConsultaService service;

    public EstudanteAtividadeController(AtividadeConsultaService service){
        this.service = service;
    }

    @Operation(
    summary = "Listar atividades do estudante",
    description = "Retorna as atividades das turmas vinculadas à matrícula informada, incluindo as concluídas.",
    responses = {
        @ApiResponse(responseCode = "200", description = "Lista de atividades do estudante retornada.", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "Estudante não encontrado.", content = @Content)})
    @GetMapping
    public List<AtividadeEstudanteResponseDTO> listar(@PathVariable String matricula){
        return service.listar(matricula);
    }


    @Operation(summary = "Consultar uma atividade do estudante", responses = {
    @ApiResponse(responseCode = "200", description = "Atividade encontrada.", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "O ID da atividade deve ser numérico.", content = @Content),
    @ApiResponse(responseCode = "403", description = "O estudante não pertence à turma da atividade.", content = @Content),
    @ApiResponse(responseCode = "404", description = "Estudante ou atividade não encontrada.", content = @Content)})
    @GetMapping("/{id}")
    public AtividadeEstudanteResponseDTO buscar(@PathVariable String matricula, @PathVariable Long id){
        return service.buscar((matricula), id);
    }
}
