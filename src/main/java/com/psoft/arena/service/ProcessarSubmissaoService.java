package com.psoft.arena.service;

import com.psoft.arena.model.StatusSubmissao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessarSubmissaoService {

    private static final Logger log = LoggerFactory.getLogger(ProcessarSubmissaoService.class);

    private final AcompanhamentoSubmissaoService acompanhamentoService;
    private final AvaliacaoService avaliacaoService;

    public ProcessarSubmissaoService(
            AcompanhamentoSubmissaoService acompanhamentoService,
            AvaliacaoService avaliacaoService) {
        this.acompanhamentoService = acompanhamentoService;
        this.avaliacaoService = avaliacaoService;
    }

    public void processar(Long submissaoId) {
        try {
            acompanhamentoService.alterarStatus(
                    submissaoId,
                    StatusSubmissao.PROCESSANDO
            );

            avaliacaoService.avaliar(submissaoId);

            acompanhamentoService.alterarStatus(
                    submissaoId,
                    StatusSubmissao.CONCLUIDA
            );
        } catch (RuntimeException ex) {
            log.warn("Falha ao processar a submissão {}: {}", submissaoId, ex.getMessage());
            try {
                acompanhamentoService.alterarStatus(
                        submissaoId,
                        StatusSubmissao.FALHA
                );
            } catch (RuntimeException ignored) {
            }
        }
    }
}
