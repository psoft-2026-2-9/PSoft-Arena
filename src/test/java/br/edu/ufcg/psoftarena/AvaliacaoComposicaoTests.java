package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.Avaliacao;
import br.edu.ufcg.psoftarena.Model.ResultadoModelo;
import br.edu.ufcg.psoftarena.Model.Submissao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Classe de testes da composição da avaliação final")
public class AvaliacaoComposicaoTests {

    ResultadoModelo resultado(String modelo, double nota, String justificativa, List<String> evidencias) {
        return ResultadoModelo.builder()
                .modelo(modelo)
                .sucesso(true)
                .nota(nota)
                .justificativa(justificativa)
                .evidencias(evidencias)
                .build();
    }

    @Test
    @DisplayName("Avaliação com um único modelo reproduz o resultado do modelo")
    void deveComporComUmModelo() {
        Submissao submissao = Submissao.builder().id(1L).build();

        Avaliacao avaliacao = Avaliacao.compor(submissao,
                List.of(resultado("grok-2", 7.5, "Boa coesão", List.of("Classe Arena coesa"))));

        assertSame(submissao, avaliacao.getSubmissao());
        assertEquals(7.5, avaliacao.getNota());
        assertEquals("Boa coesão", avaliacao.getJustificativa());
        assertEquals(List.of("Classe Arena coesa"), avaliacao.getEvidencias());
        assertNotNull(avaliacao.getDataHora());
    }

    @Test
    @DisplayName("Avaliação com múltiplos modelos usa a média das notas e reúne justificativas e evidências")
    void deveComporComMultiplosModelos() {
        Avaliacao avaliacao = Avaliacao.compor(Submissao.builder().id(1L).build(), List.of(
                resultado("modelo-a", 7, "Justificativa A", List.of("Evidência A")),
                resultado("modelo-b", 8, "Justificativa B", List.of("Evidência B1", "Evidência B2")),
                resultado("modelo-c", 8, "Justificativa C", List.of("Evidência C"))));

        assertEquals(7.67, avaliacao.getNota());
        assertEquals("Justificativa A\n\nJustificativa B\n\nJustificativa C", avaliacao.getJustificativa());
        assertEquals(List.of("Evidência A", "Evidência B1", "Evidência B2", "Evidência C"),
                avaliacao.getEvidencias());
    }
}
