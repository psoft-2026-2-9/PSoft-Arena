package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.FilaSubmissoes;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.EmFila;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Classe de testes do consumidor da fila de submissões")
class ConsumidorFilaSubmissoesTests {

    private final ConsumidorFilaSubmissoes consumidor = new ConsumidorFilaSubmissoes();

    @BeforeEach
    void setUp() {
        consumidor.avaliacaoService = mock(AvaliacaoService.class);
        consumidor.submissaoRepository = mock(SubmissaoRepository.class);
    }

    @AfterEach
    void tearDown() {
        consumidor.parar();
    }

    @Test
    @DisplayName("Processar encaminha a submissão para avaliação")
    void deveAvaliarSubmissaoRetirada() {
        consumidor.processar(5L);

        verify(consumidor.avaliacaoService).avaliarSubmissao(5L);
    }

    @Test
    @DisplayName("Erro ao avaliar uma submissão não interrompe o consumidor")
    void naoDevePropagarErroDaAvaliacao(CapturedOutput output) {
        doThrow(new IllegalStateException("erro")).when(consumidor.avaliacaoService).avaliarSubmissao(6L);

        assertDoesNotThrow(() -> consumidor.processar(6L));

        assertTrue(output.getOut().contains("Erro ao processar a submissão 6: erro"));
    }

    @Test
    @DisplayName("Ao iniciar, submissões que estavam em fila voltam para a fila")
    void deveReenfileirarSubmissoesPendentesAoIniciar() {
        when(consumidor.submissaoRepository.findAllByEstadoSubimissao(any())).thenReturn(List.of(
                Submissao.builder().id(7L).estadoSubimissao(new EmFila()).build(),
                Submissao.builder().id(8L).estadoSubimissao(new EmFila()).build()));
        FilaSubmissoes fila = mock(FilaSubmissoes.class);

        try (MockedStatic<FilaSubmissoes> singleton = mockStatic(FilaSubmissoes.class)) {
            singleton.when(FilaSubmissoes::getInstance).thenReturn(fila);
            consumidor.iniciar();
        }

        verify(fila).adicionar(7L);
        verify(fila).adicionar(8L);
    }
}
