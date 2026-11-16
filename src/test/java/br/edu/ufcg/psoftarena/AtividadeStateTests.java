package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.ENUM.StatusAtividade;
import br.edu.ufcg.psoftarena.Model.Atividade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Classe de testes do estado da atividade")
public class AtividadeStateTests {

    @Test
    @DisplayName("Atividade aberta aceita submissão")
    void atividadeAbertaDeveAceitarSubmissao() {
        Atividade atividade = Atividade.builder()
                .status(StatusAtividade.ABERTA)
                .build();

        assertTrue(atividade.aceitaSubmissao());
    }

    @Test
    @DisplayName("Atividade concluída não aceita submissão")
    void atividadeConcluidaNaoDeveAceitarSubmissao() {
        Atividade atividade = Atividade.builder()
                .status(StatusAtividade.CONCLUIDA)
                .build();

        assertFalse(atividade.aceitaSubmissao());
    }

    @Test
    @DisplayName("Conclui atividade aberta")
    void deveConcluirAtividadeAberta() {
        Atividade atividade = Atividade.builder()
                .status(StatusAtividade.ABERTA)
                .build();

        assertTrue(atividade.concluir());
        assertEquals(StatusAtividade.CONCLUIDA, atividade.getStatus());
        assertFalse(atividade.aceitaSubmissao());
    }

    @Test
    @DisplayName("Não conclui atividade já concluída")
    void naoDeveConcluirAtividadeConcluida() {
        Atividade atividade = Atividade.builder()
                .status(StatusAtividade.CONCLUIDA)
                .build();

        assertFalse(atividade.concluir());
        assertEquals(StatusAtividade.CONCLUIDA, atividade.getStatus());
    }
}
