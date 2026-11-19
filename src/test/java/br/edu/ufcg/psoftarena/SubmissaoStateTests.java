package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.State.Submissao.Concluida;
import br.edu.ufcg.psoftarena.Model.State.Submissao.EmFila;
import br.edu.ufcg.psoftarena.Model.State.Submissao.EstadoSubmissao;
import br.edu.ufcg.psoftarena.Model.State.Submissao.Falha;
import br.edu.ufcg.psoftarena.Model.State.Submissao.Processando;
import br.edu.ufcg.psoftarena.Model.State.Submissao.Submetida;
import br.edu.ufcg.psoftarena.Model.Submissao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Classe de testes do estado da submissão")
public class SubmissaoStateTests {

    final Map<String, Predicate<Submissao>> transicoes = Map.of(
            "enfileirar", Submissao::enfileirar,
            "processar", Submissao::processar,
            "concluir", Submissao::concluir,
            "falhar", Submissao::falhar);

    final Map<String, String> transicoesValidas = Map.of(
            "SUBMETIDA:enfileirar", "EMFILA",
            "EMFILA:processar", "PROCESSANDO",
            "PROCESSANDO:concluir", "CONCLUIDA",
            "PROCESSANDO:falhar", "FALHA");

    Submissao submissaoNoEstado(EstadoSubmissao estado) {
        return Submissao.builder().estadoSubmissao(estado).build();
    }

    List<EstadoSubmissao> todosOsEstados() {
        return List.of(new Submetida(), new EmFila(), new Processando(), new Concluida(), new Falha());
    }

    @Test
    @DisplayName("Nova submissão começa no estado Submetida")
    void novaSubmissaoComecaSubmetida() {
        assertEquals("SUBMETIDA", Submissao.builder().build().getEstadoSubmissao().getCodigo());
    }

    @Test
    @DisplayName("Submissão percorre o fluxo de sucesso até Concluída")
    void devePercorrerFluxoDeSucesso() {
        Submissao submissao = submissaoNoEstado(new Submetida());

        assertTrue(submissao.enfileirar());
        assertEquals("EMFILA", submissao.getEstadoSubmissao().getCodigo());
        assertTrue(submissao.processar());
        assertEquals("PROCESSANDO", submissao.getEstadoSubmissao().getCodigo());
        assertTrue(submissao.concluir());
        assertEquals("CONCLUIDA", submissao.getEstadoSubmissao().getCodigo());
    }

    @Test
    @DisplayName("Submissão em processamento passa para Falha")
    void deveFalharDuranteProcessamento() {
        Submissao submissao = submissaoNoEstado(new Processando());

        assertTrue(submissao.falhar());

        assertEquals("FALHA", submissao.getEstadoSubmissao().getCodigo());
    }

    @Test
    @DisplayName("Cada estado aceita somente as suas transições e recusa as demais sem mudar")
    void cadaEstadoAceitaSomenteSuasTransicoes() {
        for (EstadoSubmissao estado : todosOsEstados()) {
            transicoes.forEach((nome, transicao) -> {
                Submissao submissao = submissaoNoEstado(estado);
                String chave = estado.getCodigo() + ":" + nome;

                boolean mudou = transicao.test(submissao);

                if (transicoesValidas.containsKey(chave)) {
                    assertTrue(mudou, chave);
                    assertEquals(transicoesValidas.get(chave), submissao.getEstadoSubmissao().getCodigo(), chave);
                } else {
                    assertFalse(mudou, chave);
                    assertSame(estado, submissao.getEstadoSubmissao(), chave);
                }
            });
        }
    }

    @Test
    @DisplayName("Estado da submissão não pode ser nulo")
    void estadoNaoPodeSerNulo() {
        Submissao submissao = submissaoNoEstado(new Submetida());

        assertThrows(NullPointerException.class, () -> submissao.mudarEstado(null));
        assertThrows(NullPointerException.class, () -> Submissao.builder().estadoSubmissao(null).build());
        assertEquals("SUBMETIDA", submissao.getEstadoSubmissao().getCodigo());
    }
}
