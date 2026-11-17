package com.psoft.arena.controller;

import com.psoft.arena.dto.SubmissaoResponseDTO;
import com.psoft.arena.dto.AcompanhamentoSubmissaoResponseDTO;
import com.psoft.arena.service.SubmeterSolucaoService;
import com.psoft.arena.service.AcompanhamentoSubmissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/atividades/{atividadeId}/submissoes")
public class SubmissaoController {

    private final SubmeterSolucaoService submeterSolucaoService;
    private final AcompanhamentoSubmissaoService acompanhamentoService;

    public SubmissaoController(
            SubmeterSolucaoService submeterSolucaoService,
            AcompanhamentoSubmissaoService acompanhamentoService) {
        this.submeterSolucaoService = submeterSolucaoService;
        this.acompanhamentoService = acompanhamentoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissaoResponseDTO> submeterArquivo(
            @PathVariable Long atividadeId,
            @RequestParam("estudanteId") Long estudanteId,
            @RequestPart("arquivo") MultipartFile arquivo) throws IOException {

        String nomeArquivo = arquivo.getOriginalFilename();
        String codigoFonte = new String(arquivo.getBytes(), StandardCharsets.UTF_8);

        SubmissaoResponseDTO response = submeterSolucaoService.executar(
                estudanteId,
                atividadeId,
                nomeArquivo,
                codigoFonte);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{submissaoId}/acompanhamento")
    public ResponseEntity<AcompanhamentoSubmissaoResponseDTO> consultarAcompanhamento(
            @PathVariable Long atividadeId,
            @PathVariable Long submissaoId,
            @RequestParam("estudanteId") Long estudanteId) {

        return ResponseEntity.ok(
                acompanhamentoService.consultarAcompanhamento(
                        submissaoId, estudanteId, atividadeId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleUnprocessableEntity(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", ex.getMessage()));
    }
}
