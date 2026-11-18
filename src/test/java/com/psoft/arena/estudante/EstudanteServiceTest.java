package com.psoft.arena.service;

import com.psoft.arena.dto.EstudanteRequestDTO;
import com.psoft.arena.dto.EstudanteUpdateDTO;
import com.psoft.arena.exception.ConflitoException;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Estudante;
import com.psoft.arena.repository.EstudanteRepository;
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
class EstudanteServiceTest {

    @Mock
    private EstudanteRepository repository;

    @InjectMocks
    private EstudanteService service;

    private EstudanteRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new EstudanteRequestDTO();
        request.setNome(" Ana Souza ");
        request.setMatricula(" 123 ");
        request.setEmail("ana@ufcg.edu.br");
    }

    @Test
    void cadastraEstudanteNormalizandoCampos() {
        when(repository.existsByMatricula("123")).thenReturn(false);
        when(repository.save(any(Estudante.class))).thenAnswer(i -> i.getArgument(0));

        Estudante e = service.cadastrar(request);

        assertEquals("Ana Souza", e.getNome());
        assertEquals("123", e.getMatricula());
    }

    @Test
    void naoCadastraMatriculaDuplicada() {
        when(repository.existsByMatricula("123")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.cadastrar(request));
        verify(repository, never()).save(any());
    }

    @Test
    void buscaPorMatriculaInexistenteLancaErro() {
        when(repository.findByMatricula("999")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorMatricula("999"));
    }

    @Test
    void editaNomeEEmailMantendoMatricula() {
        Estudante existente = new Estudante("Ana", "123", "ana@ufcg.edu.br");
        when(repository.findByMatricula("123")).thenReturn(Optional.of(existente));
        when(repository.save(any(Estudante.class))).thenAnswer(i -> i.getArgument(0));

        EstudanteUpdateDTO dto = new EstudanteUpdateDTO();
        dto.setNome("Ana Maria");
        dto.setEmail("ana.maria@ufcg.edu.br");

        Estudante editado = service.editar("123", dto);

        assertEquals("Ana Maria", editado.getNome());
        assertEquals("ana.maria@ufcg.edu.br", editado.getEmail());
        assertEquals("123", editado.getMatricula());
    }

    @Test
    void editarEstudanteInexistenteLancaErro() {
        when(repository.findByMatricula("999")).thenReturn(Optional.empty());
        EstudanteUpdateDTO dto = new EstudanteUpdateDTO();
        dto.setNome("X");
        dto.setEmail("x@y.com");

        assertThrows(RecursoNaoEncontradoException.class, () -> service.editar("999", dto));
    }
}
