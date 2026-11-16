package com.psoft.arena.service;

import com.psoft.arena.model.StatusSubmissao;
import org.springframework.stereotype.Service;

@Service
public class ProcessarSubmissaoService {

    private final AcompanhamentoSubmissaoService acompanhamentoService;

    public ProcessarSubmissaoService(
            AcompanhamentoSubmissaoService acompanhamentoService) {
        this.acompanhamentoService = acompanhamentoService;
    }

    public void processar(Long submissaoId) {
        try {
            acompanhamentoService.alterarStatus(
                    submissaoId,
                    StatusSubmissao.PROCESSANDO
            );

            acompanhamentoService.alterarStatus(
                    submissaoId,
                    StatusSubmissao.CONCLUIDA
            );
        } catch (RuntimeException ex) {
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
