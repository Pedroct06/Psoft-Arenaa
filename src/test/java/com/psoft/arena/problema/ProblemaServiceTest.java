package com.psoft.arena.problema;

import com.psoft.arena.dto.ProblemaRequestDTO;
import com.psoft.arena.exception.ConflitoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Problema;
import com.psoft.arena.model.StatusProblema;
import com.psoft.arena.repository.ProblemaRepository;
import com.psoft.arena.service.ProblemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProblemaServiceTest {

    @Mock
    private ProblemaRepository repository;

    @InjectMocks
    private ProblemaService service;

    private ProblemaRequestDTO dto;

    @BeforeEach
    void setUp() {
        dto = new ProblemaRequestDTO();
        dto.setTitulo("Sistema de biblioteca");
        dto.setDescricao("Gerenciar empréstimos");
        dto.setRequisitosFuncionais("Cadastrar livros");
        dto.setRequisitosProjeto("Usar Spring Boot");
    }

    @Test
    void problemaNasceAtivoENaoUtilizado() {
        when(repository.save(any(Problema.class))).thenAnswer(i -> i.getArgument(0));

        Problema p = service.cadastrar(dto);

        assertEquals(StatusProblema.ATIVO, p.getStatus());
        assertFalse(p.isUtilizado());
    }

    @Test
    void buscaInexistenteLancaErro() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void editaCamposDoProblema() {
        Problema existente = new Problema("a", "b", "c", "d");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Problema.class))).thenAnswer(i -> i.getArgument(0));

        Problema editado = service.editar(1L, dto);

        assertEquals("Sistema de biblioteca", editado.getTitulo());
        assertEquals("Usar Spring Boot", editado.getRequisitosProjeto());
    }

    @Test
    void desativaProblema() {
        Problema existente = new Problema("a", "b", "c", "d");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Problema.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(StatusProblema.INATIVO, service.desativar(1L).getStatus());
    }

    @Test
    void problemaInativoNaoPodeSerUtilizado() {
        Problema existente = new Problema("a", "b", "c", "d");
        existente.desativar();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThrows(ConflitoException.class, () -> service.utilizar(1L));
        assertFalse(existente.isUtilizado());
    }

    @Test
    void problemaAtivoPodeSerUtilizado() {
        Problema existente = new Problema("a", "b", "c", "d");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Problema.class))).thenAnswer(i -> i.getArgument(0));

        assertTrue(service.utilizar(1L).isUtilizado());
    }

    @Test
    void removeProblemaNaoUtilizado() {
        Problema existente = new Problema("a", "b", "c", "d");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.remover(1L);

        verify(repository).delete(existente);
    }

    @Test
    void naoRemoveProblemaJaUtilizado() {
        Problema existente = new Problema("a", "b", "c", "d");
        existente.marcarComoUtilizado();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThrows(ConflitoException.class, () -> service.remover(1L));
        verify(repository, never()).delete(any());
    }
}
