package com.psoft.arena.submissao;

import com.psoft.arena.model.StatusSubmissao;
import com.psoft.arena.model.Submissao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubmissaoStatusTest {

    @Test
    void deveIniciarComoSubmetida() {
        Submissao submissao = new Submissao(1L, 1L, 1, "public class Main {}");

        assertEquals(StatusSubmissao.SUBMETIDA, submissao.getStatus());
    }

    @Test
    void devePermitirFluxoCompletoComSucesso() {
        Submissao submissao = new Submissao(1L, 1L, 1, "public class Main {}");

        submissao.alterarStatus(StatusSubmissao.EM_FILA);
        assertEquals(StatusSubmissao.EM_FILA, submissao.getStatus());

        submissao.alterarStatus(StatusSubmissao.PROCESSANDO);
        assertEquals(StatusSubmissao.PROCESSANDO, submissao.getStatus());

        submissao.alterarStatus(StatusSubmissao.CONCLUIDA);
        assertEquals(StatusSubmissao.CONCLUIDA, submissao.getStatus());
    }

    @Test
    void devePermitirFinalizacaoComFalha() {
        Submissao submissao = new Submissao(1L, 1L, 1, "public class Main {}");

        submissao.alterarStatus(StatusSubmissao.EM_FILA);
        submissao.alterarStatus(StatusSubmissao.PROCESSANDO);
        submissao.alterarStatus(StatusSubmissao.FALHA);

        assertEquals(StatusSubmissao.FALHA, submissao.getStatus());
    }

    @Test
    void naoDevePermitirPularEstados() {
        Submissao submissao = new Submissao(1L, 1L, 1, "public class Main {}");

        assertThrows(
                IllegalStateException.class,
                () -> submissao.alterarStatus(StatusSubmissao.PROCESSANDO)
        );
    }

    @Test
    void naoDevePermitirAlterarEstadoFinalizado() {
        Submissao submissao = new Submissao(1L, 1L, 1, "public class Main {}");

        submissao.alterarStatus(StatusSubmissao.EM_FILA);
        submissao.alterarStatus(StatusSubmissao.PROCESSANDO);
        submissao.alterarStatus(StatusSubmissao.CONCLUIDA);

        assertThrows(
                IllegalStateException.class,
                () -> submissao.alterarStatus(StatusSubmissao.EM_FILA)
        );
    }
}
