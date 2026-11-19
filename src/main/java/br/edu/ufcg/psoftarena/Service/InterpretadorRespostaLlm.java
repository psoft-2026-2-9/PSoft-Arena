package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.RespostaLlmDto;
import br.edu.ufcg.psoftarena.Exception.FalhaAvaliacaoException;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class InterpretadorRespostaLlm {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RespostaLlmDto interpretar(String resposta) {
        JsonNode json = lerJson(resposta);

        JsonNode nota = json.path("nota");
        if (!nota.isNumber() || nota.doubleValue() < 0 || nota.doubleValue() > 10) {
            throw new FalhaAvaliacaoException("Resposta do modelo sem nota válida entre 0 e 10");
        }

        JsonNode justificativa = json.path("justificativa");
        if (!justificativa.isString() || justificativa.asString().isBlank()) {
            throw new FalhaAvaliacaoException("Resposta do modelo sem justificativa");
        }

        JsonNode evidencias = json.path("evidencias");
        if (!evidencias.isArray() || evidencias.isEmpty()) {
            throw new FalhaAvaliacaoException("Resposta do modelo sem evidências");
        }
        boolean evidenciasValidas = evidencias.values().stream()
                .allMatch(evidencia -> evidencia.isString() && !evidencia.asString().isBlank());
        if (!evidenciasValidas) {
            throw new FalhaAvaliacaoException("Resposta do modelo com evidências inválidas");
        }

        return RespostaLlmDto.builder()
                .nota(nota.doubleValue())
                .justificativa(justificativa.asString())
                .evidencias(evidencias.values().stream().map(JsonNode::asString).toList())
                .build();
    }

    private JsonNode lerJson(String resposta) {
        int inicio = resposta == null ? -1 : resposta.indexOf('{');
        int fim = resposta == null ? -1 : resposta.lastIndexOf('}');
        if (inicio < 0 || fim < inicio) {
            throw new FalhaAvaliacaoException("Resposta do modelo não contém um JSON");
        }

        try {
            return objectMapper.readTree(resposta.substring(inicio, fim + 1));
        } catch (JacksonException e) {
            throw new FalhaAvaliacaoException("Resposta do modelo não é um JSON válido", e);
        }
    }
}
