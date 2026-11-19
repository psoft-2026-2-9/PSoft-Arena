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

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Classe de testes do estado da submissão")
public class SubmissaoStateTests {

    Submissao submissaoNoEstado(EstadoSubmissao estado) {
        return Submissao.builder().estadoSubmissao(estado).build();
    }

    @Test
    @DisplayName("Submissão percorre o fluxo de sucesso até Concluída")
    void devePercorrerFluxoDeSucesso() {
        Submissao submissao = submissaoNoEstado(new Submetida());

        submissao.proximoEstado();
        assertEquals("EMFILA", submissao.getEstadoSubmissao().getCodigo());
        submissao.proximoEstado();
        assertEquals("PROCESSANDO", submissao.getEstadoSubmissao().getCodigo());
        submissao.proximoEstado();
        assertEquals("CONCLUIDA", submissao.getEstadoSubmissao().getCodigo());
    }

    @Test
    @DisplayName("Submissão em processamento passa para Falha")
    void deveFalharDuranteProcessamento() {
        Submissao submissao = submissaoNoEstado(new Processando());

        submissao.falhar();

        assertEquals("FALHA", submissao.getEstadoSubmissao().getCodigo());
    }

    @Test
    @DisplayName("Somente submissões em processamento podem falhar")
    void naoDeveFalharForaDoProcessamento() {
        for (EstadoSubmissao estado : List.of(new Submetida(), new EmFila(), new Concluida(), new Falha())) {
            Submissao submissao = submissaoNoEstado(estado);

            submissao.falhar();

            assertEquals(estado.getCodigo(), submissao.getEstadoSubmissao().getCodigo());
        }
    }

    @Test
    @DisplayName("Estados finais não avançam")
    void estadosFinaisNaoDevemAvancar() {
        for (EstadoSubmissao estado : List.of(new Concluida(), new Falha())) {
            Submissao submissao = submissaoNoEstado(estado);

            submissao.proximoEstado();

            assertEquals(estado.getCodigo(), submissao.getEstadoSubmissao().getCodigo());
        }
    }
}
