package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeAberta;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeConcluida;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Classe de testes do estado da atividade")
public class AtividadeStateTests {

    @Test
    void novaAtividadeComecaAberta() {
        Atividade atividade = new Atividade();
        assertEquals("ABERTA", atividade.getStatus().getCodigo());
        assertTrue(atividade.aceitaSubmissao());
    }

    @Test
    void estadoNuloNaoReabreAtividadeConcluida() {
        AtividadeConcluida estado = new AtividadeConcluida();
        Atividade atividade = Atividade.builder().status(estado).build();

        assertThrows(NullPointerException.class, () -> atividade.mudarEstado(null));

        assertSame(estado, atividade.getStatus());
        assertFalse(atividade.aceitaSubmissao());
    }

    @Test
    void builderNaoAceitaEstadoNulo() {
        assertThrows(NullPointerException.class, () -> Atividade.builder().status(null).build());
        assertTrue(Atividade.builder().build().aceitaSubmissao());
    }

    @Test
    @DisplayName("Atividade aberta aceita submissão")
    void atividadeAbertaDeveAceitarSubmissao() {
        Atividade atividade = Atividade.builder()
                .status(new AtividadeAberta())
                .build();

        assertTrue(atividade.aceitaSubmissao());
    }

    @Test
    @DisplayName("Atividade concluída não aceita submissão")
    void atividadeConcluidaNaoDeveAceitarSubmissao() {
        Atividade atividade = Atividade.builder()
                .status(new AtividadeConcluida())
                .build();

        assertFalse(atividade.aceitaSubmissao());
    }

    @Test
    @DisplayName("Conclui atividade aberta")
    void deveConcluirAtividadeAberta() {
        Atividade atividade = Atividade.builder()
                .status(new AtividadeAberta())
                .build();

        assertTrue(atividade.concluir());
        assertEquals("CONCLUIDA", atividade.getStatus().getCodigo());
        assertFalse(atividade.aceitaSubmissao());
    }

    @Test
    @DisplayName("Não conclui atividade já concluída")
    void naoDeveConcluirAtividadeConcluida() {
        Atividade atividade = Atividade.builder()
                .status(new AtividadeConcluida())
                .build();

        assertFalse(atividade.concluir());
        assertEquals("CONCLUIDA", atividade.getStatus().getCodigo());
    }
}
