package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.State.Subimissao.Concluida;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.EmFila;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.EstadoSubimissao;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.Falha;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.Processando;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.Submetida;
import br.edu.ufcg.psoftarena.Model.Submissao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Classe de testes do estado da submissão")
public class SubmissaoStateTests {

    Submissao submissaoNoEstado(EstadoSubimissao estado) {
        return Submissao.builder().estadoSubimissao(estado).build();
    }

    @Test
    @DisplayName("Submissão percorre o fluxo de sucesso até Concluída")
    void devePercorrerFluxoDeSucesso() {
        Submissao submissao = submissaoNoEstado(new Submetida());

        submissao.proximoEstado();
        assertEquals("EMFILA", submissao.getEstadoSubimissao().getCodigo());
        submissao.proximoEstado();
        assertEquals("PROCESSANDO", submissao.getEstadoSubimissao().getCodigo());
        submissao.proximoEstado();
        assertEquals("CONCLUIDA", submissao.getEstadoSubimissao().getCodigo());
    }

    @Test
    @DisplayName("Submissão em processamento passa para Falha")
    void deveFalharDuranteProcessamento() {
        Submissao submissao = submissaoNoEstado(new Processando());

        submissao.falhar();

        assertEquals("FALHA", submissao.getEstadoSubimissao().getCodigo());
    }

    @Test
    @DisplayName("Somente submissões em processamento podem falhar")
    void naoDeveFalharForaDoProcessamento() {
        for (EstadoSubimissao estado : List.of(new Submetida(), new EmFila(), new Concluida(), new Falha())) {
            Submissao submissao = submissaoNoEstado(estado);

            submissao.falhar();

            assertEquals(estado.getCodigo(), submissao.getEstadoSubimissao().getCodigo());
        }
    }

    @Test
    @DisplayName("Estados finais não avançam")
    void estadosFinaisNaoDevemAvancar() {
        for (EstadoSubimissao estado : List.of(new Concluida(), new Falha())) {
            Submissao submissao = submissaoNoEstado(estado);

            submissao.proximoEstado();

            assertEquals(estado.getCodigo(), submissao.getEstadoSubimissao().getCodigo());
        }
    }
}
