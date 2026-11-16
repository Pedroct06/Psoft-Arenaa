package com.psoft.arena.repository;

import com.psoft.arena.model.Problema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemaRepository extends JpaRepository<Problema, Long> {
}
