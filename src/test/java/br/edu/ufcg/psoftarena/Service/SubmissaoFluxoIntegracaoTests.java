package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Client.ClienteLlm;
import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import br.edu.ufcg.psoftarena.Exception.FalhaAvaliacaoException;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import br.edu.ufcg.psoftarena.Model.FilaSubmissoes;
import br.edu.ufcg.psoftarena.Model.HistoricoEstadoSubmissao;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeAberta;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.AvaliacaoRepository;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.EstrategiaRepository;
import br.edu.ufcg.psoftarena.Repository.HistoricoEstadoSubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import br.edu.ufcg.psoftarena.Repository.ResultadoModeloRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SubmissaoFluxoIntegracaoTests.ConfiguracaoFila.class)
@DisplayName("Fluxo HTTP de submissão com fila e avaliação assíncrona reais")
class SubmissaoFluxoIntegracaoTests {

    final AtomicLong contador = new AtomicLong();

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ClienteLlm clienteLlm;

    // Os outros contextos Spring usam o Singleton; este contexto possui sua própria fila real.
    @MockitoBean(name = "consumidorFilaSubmissoes")
    ConsumidorFilaSubmissoes consumidorPadrao;

    @TestConfiguration
    static class ConfiguracaoFila {
        @Bean
        FilaSubmissoes filaIsolada() throws ReflectiveOperationException {
            var construtor = FilaSubmissoes.class.getDeclaredConstructor();
            construtor.setAccessible(true);
            return construtor.newInstance();
        }

        @Bean
        @Primary
        EnfileiradorSubmissoes enfileiradorIntegracao(FilaSubmissoes fila) {
            return new EnfileiradorSubmissoes(fila);
        }

        @Bean
        @Primary
        ConsumidorFilaSubmissoes consumidorIntegracao(FilaSubmissoes fila) {
            return new ConsumidorFilaSubmissoes(fila);
        }
    }

    @Autowired
    SubmissaoRepository submissaoRepository;

    @Autowired
    AvaliacaoRepository avaliacaoRepository;

    @Autowired
    ResultadoModeloRepository resultadoModeloRepository;

    @Autowired
    HistoricoEstadoSubmissaoRepository historicoRepository;

    @Autowired
    AtividadeRepository atividadeRepository;

    @Autowired
    TurmaRepository turmaRepository;

    @Autowired
    EstudanteRepository estudanteRepository;

    @Autowired
    ProblemaRepository problemaRepository;

    @Autowired
    EstrategiaRepository estrategiaRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    TextEncryptor textEncryptor;

    Estudante estudante;
    Atividade atividade;

    @BeforeEach
    void setUp() {
        Professor professor = professorRepository.save(Professor.builder().nome("Professor A").build());
        estudante = estudanteRepository.save(Estudante.builder()
                .nome("Estudante A")
                .matricula("2026" + contador.incrementAndGet())
                .email("estudante" + contador.get() + "@ufcg.edu.br")
                .build());
        Turma turma = Turma.builder()
                .nome("Turma A")
                .codigo("TURMA-AVALIACAO-" + contador.incrementAndGet())
                .periodoLetivo("2026.2")
                .professor(professor)
                .status(StatusTurma.ATIVA)
                .build();
        turma.getEstudantes().add(estudante);
        turma = turmaRepository.save(turma);

        Problema problema = problemaRepository.save(Problema.builder()
                .titulo("Arena de Batalha")
                .descricao("Modele uma arena onde jogadores se enfrentam")
                .requisitosFuncionais(List.of("Cadastrar jogador", "Iniciar batalha"))
                .requisitosDeProjeto(List.of("Usar o padrão Strategy"))
                .status(StatusProblema.ATIVO)
                .build());
        Estrategia estrategia = estrategiaRepository.save(Estrategia.builder()
                .nome("Coesão e Acoplamento")
                .tipo(TipoEstrategia.BAIXO_ACOPLAMENTO_ALTA_COESAO_PRINCIPIOS)
                .descricao("Avalia coesão e acoplamento")
                .prompt("Verifique se as classes possuem alta coesão e baixo acoplamento.")
                .build());

        atividade = atividadeRepository.save(Atividade.builder()
                .titulo("Atividade 1")
                .descricao("Implemente a arena")
                .prazo(LocalDateTime.now().plusDays(7))
                .status(new AtividadeAberta())
                .turma(turma)
                .problema(problema)
                .estrategia(estrategia)
                .llmModelo("grok-2")
                .llmUrlApi("https://api.x.ai/v1")
                .llmChaveApiCriptografada(textEncryptor.encrypt("chave-real-da-api"))
                .build());
    }

    @AfterEach
    void tearDown() {
        resultadoModeloRepository.deleteAll();
        avaliacaoRepository.deleteAll();
        historicoRepository.deleteAll();
        submissaoRepository.deleteAll();
        atividadeRepository.deleteAll();
        turmaRepository.deleteAll();
        estudanteRepository.deleteAll();
        problemaRepository.deleteAll();
        estrategiaRepository.deleteAll();
        professorRepository.deleteAll();
    }

    @Test
    @DisplayName("POST percorre a fila em outra thread e GET apresenta Concluída e avaliação persistida")
    void deveConcluirSubmissaoPelaFila() throws Exception {
        executarFluxo(false);
    }

    @Test
    @DisplayName("Erro do LLM após o POST resulta em Falha consultável e erro persistido")
    void deveRegistrarFalhaPelaFila() throws Exception {
        executarFluxo(true);
    }

    private void executarFluxo(boolean falha) throws Exception {
        CountDownLatch iniciouLlm = new CountDownLatch(1);
        CountDownLatch liberarLlm = new CountDownLatch(1);
        AtomicReference<Thread> threadAvaliacao = new AtomicReference<>();
        when(clienteLlm.enviarPrompt(any(), anyString())).thenAnswer(invocacao -> {
            threadAvaliacao.set(Thread.currentThread());
            iniciouLlm.countDown();
            if (!liberarLlm.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Teste não liberou a resposta do LLM");
            }
            if (falha) {
                throw new FalhaAvaliacaoException("API indisponível");
            }
            return """
                    {"nota": 8.5, "justificativa": "Solução coesa", "evidencias": ["Responsabilidades separadas"]}
                    """;
        });

        long id;
        try {
            String resposta = mockMvc.perform(post("/atividades/{id}/submissoes", atividade.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"estudante_id": %d, "codigo_fonte": "public class Solucao {}"}
                                    """.formatted(estudante.getId())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("EMFILA"))
                    .andExpect(jsonPath("$.versao").value(1))
                    .andReturn().getResponse().getContentAsString();
            id = objectMapper.readTree(resposta).get("id").asLong();
            assertTrue(iniciouLlm.await(5, TimeUnit.SECONDS), "Consumidor não iniciou a avaliação");
            assertNotSame(Thread.currentThread(), threadAvaliacao.get(), "Avaliação deve ocorrer em outra thread");
            consultar(id, "PROCESSANDO");
        } finally {
            liberarLlm.countDown();
        }

        String estadoFinal = falha ? "FALHA" : "CONCLUIDA";
        aguardarEstadoFinal(id, estadoFinal);
        consultar(id, estadoFinal);
        assertEquals(List.of("SUBMETIDA", "EMFILA", "PROCESSANDO", estadoFinal),
                historicoRepository.findBySubmissaoIdOrderByDataHoraAscIdAsc(id).stream()
                        .map(HistoricoEstadoSubmissao::getEstadoNovo).toList());

        var resultados = resultadoModeloRepository.findBySubmissaoIdOrderByIdAsc(id);
        assertEquals(1, resultados.size());
        assertEquals(!falha, resultados.getFirst().isSucesso());
        if (falha) {
            assertTrue(avaliacaoRepository.findBySubmissaoId(id).isEmpty());
            assertEquals("API indisponível", resultados.getFirst().getErro());
        } else {
            var avaliacao = avaliacaoRepository.findBySubmissaoId(id).orElseThrow();
            assertEquals(8.5, avaliacao.getNota());
            assertEquals("Solução coesa", avaliacao.getJustificativa());
            assertEquals(List.of("Responsabilidades separadas"), avaliacao.getEvidencias());
            assertEquals(1, avaliacaoRepository.count());
        }
        verify(clienteLlm, times(1)).enviarPrompt(any(), anyString());
    }

    private void consultar(long id, String estado) throws Exception {
        mockMvc.perform(get("/atividades/{atividadeId}/submissoes/{id}", atividade.getId(), id)
                        .param("estudanteId", estudante.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value(estado));
    }

    private void aguardarEstadoFinal(long id, String esperado) throws InterruptedException {
        long limite = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (System.nanoTime() < limite) {
            String atual = submissaoRepository.findById(id).orElseThrow().getEstadoSubimissao().getCodigo();
            if (esperado.equals(atual)) {
                return;
            }
            Thread.sleep(20);
        }
        fail("Submissão não chegou ao estado " + esperado);
    }
}
