package com.psoft.arena.repository;

import com.psoft.arena.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
    Optional<Turma> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
