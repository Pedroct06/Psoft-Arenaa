package com.psoft.arena.repository;

import com.psoft.arena.model.Estudante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstudanteRepository extends JpaRepository<Estudante, Long> {
    Optional<Estudante> findByMatricula(String matricula);
    boolean existsByMatricula(String matricula);
}
