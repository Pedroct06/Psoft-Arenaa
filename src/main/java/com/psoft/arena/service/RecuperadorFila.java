package com.psoft.arena.service;

import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.model.Submissao;
import com.psoft.arena.repository.SubmissaoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RecuperadorFila {

  private final SubmissaoRepository submissaoRepository;
  private final AcompanhamentoSubmissaoService acompanhamentoService;
  private final ProcessarSubmissaoService processarService;
  private final ThreadPoolTaskExecutor executor;

  public RecuperadorFila(
      SubmissaoRepository submissaoRepository,
      AcompanhamentoSubmissaoService acompanhamentoService,
      ProcessarSubmissaoService processarService,
      @Qualifier("filaSubmissaoExecutor") ThreadPoolTaskExecutor executor) {
    this.submissaoRepository = submissaoRepository;
    this.acompanhamentoService = acompanhamentoService;
    this.processarService = processarService;
    this.executor = executor;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void recuperar() {
    // 1. PROCESSANDO -> FALHA
    List<Submissao> processando = submissaoRepository.findByStatus(StatusSubmissao.PROCESSANDO);
    for (Submissao submissao : processando) {
      acompanhamentoService.alterarStatus(submissao.getId(), StatusSubmissao.FALHA);
    }

    // 2. SUBMETIDA -> EM_FILA
    List<Submissao> submetidas = submissaoRepository.findByStatus(StatusSubmissao.SUBMETIDA);
    for (Submissao submissao : submetidas) {
      acompanhamentoService.alterarStatus(submissao.getId(), StatusSubmissao.EM_FILA);
    }

    // 3. EM_FILA (antigas + recém-promovidas de SUBMETIDA) -> volta para a fila
    List<Submissao> emFila = submissaoRepository.findByStatus(StatusSubmissao.EM_FILA);
    emFila.stream()
        .sorted(Comparator.comparing(Submissao::getId))
        .forEach(submissao -> executor.execute(() -> processarService.processar(submissao.getId())));
  }
}