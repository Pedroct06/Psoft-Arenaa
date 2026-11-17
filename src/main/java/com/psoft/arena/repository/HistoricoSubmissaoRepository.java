package com.psoft.arena.repository;

import com.psoft.arena.model.HistoricoSubmissao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoSubmissaoRepository
        extends JpaRepository<HistoricoSubmissao, Long> {

    List<HistoricoSubmissao> findBySubmissaoIdOrderByDataHoraAsc(Long submissaoId);
}
