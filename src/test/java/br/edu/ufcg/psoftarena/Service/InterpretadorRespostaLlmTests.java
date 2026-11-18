package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.RespostaLlmDto;
import br.edu.ufcg.psoftarena.Exception.FalhaAvaliacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Classe de testes do interpretador da resposta do LLM")
class InterpretadorRespostaLlmTests {

    private final InterpretadorRespostaLlm interpretador = new InterpretadorRespostaLlm();

    @Test
    @DisplayName("Interpreta resposta JSON válida")
    void deveInterpretarRespostaValida() {
        RespostaLlmDto resposta = interpretador.interpretar(
                "{\"nota\": 8.5, \"justificativa\": \"Bom uso de interfaces\", \"evidencias\": [\"Arena usa Jogador\"]}");

        assertEquals(8.5, resposta.getNota());
        assertEquals("Bom uso de interfaces", resposta.getJustificativa());
        assertEquals(List.of("Arena usa Jogador"), resposta.getEvidencias());
    }

    @Test
    @DisplayName("Interpreta JSON cercado por texto e bloco de código")
    void deveInterpretarJsonCercadoPorTexto() {
        RespostaLlmDto resposta = interpretador.interpretar("""
                Segue a avaliação:
                ```json
                {"nota": 10, "justificativa": "Excelente", "evidencias": ["Uso de Strategy"]}
                ```
                """);

        assertEquals(10.0, resposta.getNota());
        assertEquals(List.of("Uso de Strategy"), resposta.getEvidencias());
    }

    @Test
    @DisplayName("Aceita notas nos limites de 0 e 10")
    void deveAceitarNotasNosLimites() {
        for (String nota : List.of("0", "10")) {
            RespostaLlmDto resposta = interpretador.interpretar(
                    "{\"nota\": " + nota + ", \"justificativa\": \"J\", \"evidencias\": [\"E\"]}");
            assertEquals(Double.parseDouble(nota), resposta.getNota());
        }
    }

    @Test
    @DisplayName("Rejeita respostas que não produzem um resultado válido")
    void deveRejeitarRespostasInvalidas() {
        List<String> respostasInvalidas = List.of(
                "",
                "Não consigo avaliar este código.",
                "{\"nota\": 8, \"justificativa\": ",
                "{\"justificativa\": \"J\", \"evidencias\": [\"E\"]}",
                "{\"nota\": \"oito\", \"justificativa\": \"J\", \"evidencias\": [\"E\"]}",
                "{\"nota\": -1, \"justificativa\": \"J\", \"evidencias\": [\"E\"]}",
                "{\"nota\": 10.5, \"justificativa\": \"J\", \"evidencias\": [\"E\"]}",
                "{\"nota\": 8, \"evidencias\": [\"E\"]}",
                "{\"nota\": 8, \"justificativa\": \"  \", \"evidencias\": [\"E\"]}",
                "{\"nota\": 8, \"justificativa\": \"J\"}",
                "{\"nota\": 8, \"justificativa\": \"J\", \"evidencias\": []}",
                "{\"nota\": 8, \"justificativa\": \"J\", \"evidencias\": [\" \"]}",
                "{\"nota\": 8, \"justificativa\": 42, \"evidencias\": [\"E\"]}",
                "{\"nota\": 8, \"justificativa\": [\"J\"], \"evidencias\": [\"E\"]}",
                "{\"nota\": 8, \"justificativa\": \"J\", \"evidencias\": [123]}",
                "{\"nota\": 8, \"justificativa\": \"J\", \"evidencias\": [\"E\", 123]}",
                "{\"nota\": 8, \"justificativa\": \"J\", \"evidencias\": [\"E\", \" \"]}",
                "{\"nota\": 8, \"justificativa\": \"J\", \"evidencias\": \"E\"}",
                "{\"nota\": 8, \"justificativa\": \"J\", \"evidencias\": {\"a\": \"E\"}}",
                "{\"nota\":8,\"justificativa\":42,\"evidencias\":[123]}");

        for (String resposta : respostasInvalidas) {
            assertThrows(FalhaAvaliacaoException.class, () -> interpretador.interpretar(resposta), resposta);
        }
        assertThrows(FalhaAvaliacaoException.class, () -> interpretador.interpretar(null));
    }
}
