package com.psoft.arena.turma;

import com.psoft.arena.dto.TurmaRequestDTO;
import com.psoft.arena.dto.TurmaUpdateDTO;
import com.psoft.arena.exception.ConflitoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.StatusTurma;
import com.psoft.arena.model.Turma;
import com.psoft.arena.repository.TurmaRepository;
import com.psoft.arena.service.TurmaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {

    @Mock
    private TurmaRepository repository;

    @InjectMocks
    private TurmaService service;

    private TurmaRequestDTO dto;

    @BeforeEach
    void setUp() {
        dto = new TurmaRequestDTO();
        dto.setNome("Projeto de Software");
        dto.setCodigo(" PSOFT-01 ");
        dto.setPeriodoLetivo("2026.2");
        dto.setProfessorResponsavel("Prof. Fulano");
    }

    @Test
    void turmaNasceAtiva() {
        when(repository.existsByCodigo("PSOFT-01")).thenReturn(false);
        when(repository.save(any(Turma.class))).thenAnswer(i -> i.getArgument(0));

        Turma t = service.cadastrar(dto);

        assertEquals(StatusTurma.ATIVA, t.getStatus());
        assertEquals("PSOFT-01", t.getCodigo());
    }

    @Test
    void naoCadastraCodigoDuplicado() {
        when(repository.existsByCodigo("PSOFT-01")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.cadastrar(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void buscaInexistenteLancaErro() {
        when(repository.findByCodigo("X")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorCodigo("X"));
    }

    @Test
    void editaMantendoCodigo() {
        Turma existente = new Turma("a", "PSOFT-01", "2026.1", "p");
        when(repository.findByCodigo("PSOFT-01")).thenReturn(Optional.of(existente));
        when(repository.save(any(Turma.class))).thenAnswer(i -> i.getArgument(0));
        TurmaUpdateDTO up = new TurmaUpdateDTO();
        up.setNome("Novo nome");
        up.setPeriodoLetivo("2026.2");
        up.setProfessorResponsavel("Outro prof.");

        Turma editada = service.editar("PSOFT-01", up);

        assertEquals("Novo nome", editada.getNome());
        assertEquals("PSOFT-01", editada.getCodigo());
    }

    @Test
    void encerraTurma() {
        Turma existente = new Turma("a", "PSOFT-01", "2026.1", "p");
        when(repository.findByCodigo("PSOFT-01")).thenReturn(Optional.of(existente));
        when(repository.save(any(Turma.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(StatusTurma.ENCERRADA, service.encerrar("PSOFT-01").getStatus());
    }

    @Test
    void turmaEncerradaAindaPodeSerConsultada() {
        Turma existente = new Turma("a", "PSOFT-01", "2026.1", "p");
        existente.encerrar();
        when(repository.findByCodigo("PSOFT-01")).thenReturn(Optional.of(existente));

        assertEquals(StatusTurma.ENCERRADA, service.buscarPorCodigo("PSOFT-01").getStatus());
    }

    @Test
    void turmaEncerradaNaoRecebeNovasAssociacoes() {
        Turma existente = new Turma("a", "PSOFT-01", "2026.1", "p");
        existente.encerrar();
        when(repository.findByCodigo("PSOFT-01")).thenReturn(Optional.of(existente));

        assertThrows(ConflitoException.class, () -> service.garantirTurmaAtiva("PSOFT-01"));
    }

    @Test
    void turmaAtivaPassaNaVerificacao() {
        Turma existente = new Turma("a", "PSOFT-01", "2026.1", "p");
        when(repository.findByCodigo("PSOFT-01")).thenReturn(Optional.of(existente));

        assertEquals("PSOFT-01", service.garantirTurmaAtiva("PSOFT-01").getCodigo());
    }
}
