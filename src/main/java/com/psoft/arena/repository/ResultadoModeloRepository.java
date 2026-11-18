package com.psoft.arena.repository;

import com.psoft.arena.model.ResultadoModelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultadoModeloRepository extends JpaRepository<ResultadoModelo, Long> {

    List<ResultadoModelo> findBySubmissaoIdOrderByDataHoraAsc(Long submissaoId);
}
