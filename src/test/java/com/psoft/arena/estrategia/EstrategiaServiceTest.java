package com.psoft.arena.estrategia;

import com.psoft.arena.dto.EstrategiaRequestDTO;
import com.psoft.arena.exception.RecursoNaoEncontradoException;
import com.psoft.arena.model.Estrategia;
import com.psoft.arena.model.TipoEstrategia;
import com.psoft.arena.repository.EstrategiaRepository;
import com.psoft.arena.service.EstrategiaService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstrategiaServiceTest {

    @Mock
    private EstrategiaRepository repository;

    @InjectMocks
    private EstrategiaService service;

    private EstrategiaRequestDTO dto;

    @BeforeEach
    void setUp() {
        dto = new EstrategiaRequestDTO();
        dto.setNome(" Detector de Long Method ");
        dto.setTipo(TipoEstrategia.CODE_SMELLS);
        dto.setDescricao("Procura métodos longos");
        dto.setPromptAvaliacao("Analise o código e aponte métodos longos.");
    }

    @Test
    void cadastraEstrategiaNormalizandoCampos() {
        when(repository.save(any(Estrategia.class))).thenAnswer(i -> i.getArgument(0));

        Estrategia e = service.cadastrar(dto);

        assertEquals("Detector de Long Method", e.getNome());
        assertEquals(TipoEstrategia.CODE_SMELLS, e.getTipo());
    }

    @Test
    void permiteVariasEstrategiasDoMesmoTipo() {
        when(repository.save(any(Estrategia.class))).thenAnswer(i -> i.getArgument(0));

        Estrategia a = service.cadastrar(dto);
        dto.setNome("Outra de Code Smells");
        dto.setPromptAvaliacao("Outro prompt");
        Estrategia b = service.cadastrar(dto);

        assertEquals(a.getTipo(), b.getTipo());
        assertEquals("Outro prompt", b.getPromptAvaliacao());
    }

    @Test
    void buscaInexistenteLancaErro() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void alteraPromptDaEstrategia() {
        Estrategia existente = new Estrategia("n", TipoEstrategia.CODE_SMELLS, "d", "prompt antigo");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Estrategia.class))).thenAnswer(i -> i.getArgument(0));

        Estrategia editada = service.editar(1L, dto);

        assertEquals("Analise o código e aponte métodos longos.", editada.getPromptAvaliacao());
    }
}
