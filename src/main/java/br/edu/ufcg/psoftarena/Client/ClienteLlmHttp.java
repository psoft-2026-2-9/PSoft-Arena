package br.edu.ufcg.psoftarena.Client;

import br.edu.ufcg.psoftarena.Exception.FalhaAvaliacaoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class ClienteLlmHttp implements ClienteLlm {

    private static final String CAMINHO_CHAT = "/chat/completions";

    private final RestClient restClient;

    public ClienteLlmHttp(@Value("${llm.timeout-segundos:60}") long timeoutSegundos) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSegundos));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
        this.restClient = RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(requestFactory))
                .build();
    }

    @Override
    public String enviarPrompt(ConfiguracaoLlm configuracao, String prompt) {
        JsonNode resposta;
        try {
            resposta = restClient.post()
                    .uri(montarUrl(configuracao.getUrlApi()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + configuracao.getChaveApi())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", configuracao.getModelo(),
                            "temperature", 0,
                            "messages", List.of(Map.of("role", "user", "content", prompt))))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new FalhaAvaliacaoException(
                    "Falha na comunicação com o modelo " + configuracao.getModelo() + ": " + e.getMessage(), e);
        }

        String conteudo = resposta == null ? ""
                : resposta.path("choices").path(0).path("message").path("content").asString("");
        if (conteudo.isBlank()) {
            throw new FalhaAvaliacaoException(
                    "O modelo " + configuracao.getModelo() + " não retornou uma resposta");
        }
        return conteudo;
    }

    private String montarUrl(String urlApi) {
        String url = urlApi.endsWith("/") ? urlApi.substring(0, urlApi.length() - 1) : urlApi;
        return url.endsWith(CAMINHO_CHAT) ? url : url + CAMINHO_CHAT;
    }
}
