package com.psoft.arena.repository;

import com.psoft.arena.model.Atividade;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    List<Atividade> findByTurmaEstudantesMatriculaOrderByPrazoSubmissaoAsc(String matricula);

    // Já traz problema e estratégia, senão dá LazyInitializationException na thread da fila
    @EntityGraph(attributePaths = {"problema", "estrategia"})
    Optional<Atividade> findParaAvaliacaoById(Long id);

}
