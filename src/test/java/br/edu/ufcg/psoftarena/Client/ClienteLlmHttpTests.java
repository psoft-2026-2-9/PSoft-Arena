package br.edu.ufcg.psoftarena.Client;

import br.edu.ufcg.psoftarena.Exception.FalhaAvaliacaoException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Classe de testes do cliente HTTP de LLM")
class ClienteLlmHttpTests {

    private static final String RESPOSTA_SUCESSO =
            "{\"choices\": [{\"message\": {\"role\": \"assistant\", \"content\": \"{\\\"nota\\\": 9}\"}}]}";

    private HttpServer servidor;
    private final AtomicReference<String> caminhoRecebido = new AtomicReference<>();
    private final AtomicReference<String> autorizacaoRecebida = new AtomicReference<>();
    private final AtomicReference<String> corpoRecebido = new AtomicReference<>();
    private final AtomicReference<String> tamanhoRecebido = new AtomicReference<>();
    private final ClienteLlmHttp cliente = new ClienteLlmHttp(1);

    @BeforeEach
    void setUp() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.start();
    }

    @AfterEach
    void tearDown() {
        servidor.stop(0);
    }

    void responder(int status, String corpo) {
        responder("/", status, corpo);
    }

    void responder(String contexto, int status, String corpo) {
        servidor.createContext(contexto, exchange -> {
            registrarRequisicao(exchange);
            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }

    void registrarRequisicao(HttpExchange exchange) throws IOException {
        caminhoRecebido.set(exchange.getRequestURI().getPath());
        autorizacaoRecebida.set(exchange.getRequestHeaders().getFirst("Authorization"));
        corpoRecebido.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        tamanhoRecebido.set(exchange.getRequestHeaders().getFirst("Content-Length"));
    }

    ConfiguracaoLlm configuracao(String urlApi) {
        return ConfiguracaoLlm.builder()
                .modelo("grok-2")
                .urlApi(urlApi)
                .chaveApi("chave-secreta")
                .build();
    }

    String urlBase() {
        return "http://localhost:" + servidor.getAddress().getPort() + "/v1";
    }

    @Test
    @DisplayName("Envia o prompt ao endpoint de chat e retorna o conteúdo da resposta")
    void deveEnviarPromptERetornarConteudo() {
        responder(200, RESPOSTA_SUCESSO);

        String resposta = cliente.enviarPrompt(configuracao(urlBase()), "Avalie este código");

        assertEquals("{\"nota\": 9}", resposta);
        assertEquals("/v1/chat/completions", caminhoRecebido.get());
        assertEquals("Bearer chave-secreta", autorizacaoRecebida.get());
        assertTrue(corpoRecebido.get().contains("\"model\":\"grok-2\""));
        assertTrue(corpoRecebido.get().contains("Avalie este código"));
        assertEquals(String.valueOf(corpoRecebido.get().getBytes(StandardCharsets.UTF_8).length), tamanhoRecebido.get());
    }

    @Test
    @DisplayName("Aceita URL que já aponta para o endpoint de chat")
    void deveAceitarUrlCompleta() {
        responder(200, RESPOSTA_SUCESSO);

        cliente.enviarPrompt(configuracao(urlBase() + "/chat/completions/"), "Avalie");

        assertEquals("/v1/chat/completions", caminhoRecebido.get());
    }

    @Test
    @DisplayName("Erro retornado pela API gera falha na avaliação")
    void deveFalharQuandoApiRetornaErro() {
        for (int status : new int[]{401, 429, 500}) {
            responder("/erro" + status, status, "{\"error\": \"falha\"}");
            String url = "http://localhost:" + servidor.getAddress().getPort() + "/erro" + status;

            assertThrows(FalhaAvaliacaoException.class,
                    () -> cliente.enviarPrompt(configuracao(url), "Avalie"));
        }
    }

    @Test
    @DisplayName("Resposta sem conteúdo gera falha na avaliação")
    void deveFalharQuandoRespostaSemConteudo() {
        responder(200, "{\"choices\": []}");

        assertThrows(FalhaAvaliacaoException.class,
                () -> cliente.enviarPrompt(configuracao(urlBase()), "Avalie"));
    }

    @Test
    @DisplayName("Modelo que não responde dentro do tempo limite gera falha na avaliação")
    void deveFalharQuandoModeloNaoResponde() {
        servidor.createContext("/", exchange -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });

        assertThrows(FalhaAvaliacaoException.class,
                () -> cliente.enviarPrompt(configuracao(urlBase()), "Avalie"));
    }

    @Test
    @DisplayName("Falha de comunicação com o serviço gera falha na avaliação")
    void deveFalharQuandoServicoIndisponivel() throws IOException {
        int portaLivre;
        try (ServerSocket socket = new ServerSocket(0)) {
            portaLivre = socket.getLocalPort();
        }
        String url = "http://localhost:" + portaLivre + "/v1";

        assertThrows(FalhaAvaliacaoException.class,
                () -> cliente.enviarPrompt(configuracao(url), "Avalie"));
    }
}
