package com.psoft.arena.repository;

import com.psoft.arena.model.Atividade;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    List<Atividade> findByTurmaEstudantesMatriculaOrderByPrazoSubmissaoAsc(String matricula);

}
