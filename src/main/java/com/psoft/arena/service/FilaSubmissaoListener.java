package com.psoft.arena.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FilaSubmissaoListener {

    private final AcompanhamentoSubmissaoService acompanhamentoService;
    private final ProcessarSubmissaoService processarService;
    private final ThreadPoolTaskExecutor executor;

    public FilaSubmissaoListener(
            AcompanhamentoSubmissaoService acompanhamentoService,
            ProcessarSubmissaoService processarService,
            @Qualifier("filaSubmissaoExecutor") ThreadPoolTaskExecutor executor) {
        this.acompanhamentoService = acompanhamentoService;
        this.processarService = processarService;
        this.executor = executor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoReceberSubmissao(SubmissaoRecebidaEvent evento) {
        acompanhamentoService.alterarStatus(
                evento.submissaoId(),
                com.psoft.arena.model.StatusSubmissao.EM_FILA
        );

        executor.execute(() ->
                processarService.processar(evento.submissaoId())
        );
    }
}
