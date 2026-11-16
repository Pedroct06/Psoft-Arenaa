package com.psoft.arena.repository;

import com.psoft.arena.model.Submissao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissaoRepository extends JpaRepository<Submissao, Long> {
  int countByEstudanteIdAndAtividadeId(Long estudanteId, Long atividadeId);
  List<Submissao> findByEstudanteIdAndAtividadeIdOrderByVersaoAsc(Long estudanteId, Long atividadeId);
}
