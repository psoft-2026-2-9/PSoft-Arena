package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Client.ClienteLlm;
import br.edu.ufcg.psoftarena.Client.ConfiguracaoLlm;
import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import br.edu.ufcg.psoftarena.Exception.FalhaAvaliacaoException;
import br.edu.ufcg.psoftarena.Exception.RecursoNaoEncontradoException;
import br.edu.ufcg.psoftarena.Exception.RegraNegocioException;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Avaliacao;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Model.ResultadoModelo;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeAberta;
import br.edu.ufcg.psoftarena.Model.State.Submissao.EmFila;
import br.edu.ufcg.psoftarena.Model.State.Submissao.EstadoSubmissao;
import br.edu.ufcg.psoftarena.Model.State.Submissao.Processando;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.AvaliacaoRepository;
import br.edu.ufcg.psoftarena.Repository.EstrategiaRepository;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.HistoricoEstadoSubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import br.edu.ufcg.psoftarena.Repository.ResultadoModeloRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import br.edu.ufcg.psoftarena.Service.AvaliacaoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Classe de testes da avaliação de submissões")
public class AvaliacaoServiceTests {

    final String CODIGO_FONTE = "public class Arena { private List<Jogador> jogadores; }";

    final String RESPOSTA_VALIDA = """
            {"nota": 8.5, "justificativa": "Classes coesas e baixo acoplamento",
             "evidencias": ["Arena depende da interface Jogador", "Nenhum método longo"]}
            """;

    final AtomicLong contador = new AtomicLong();

    @Autowired
    AvaliacaoService avaliacaoService;

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

    @MockitoBean
    ClienteLlm clienteLlm;

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

    Submissao salvarSubmissao(EstadoSubmissao estado) {
        return submissaoRepository.save(Submissao.builder()
                .estudante(estudante)
                .atividade(atividade)
                .versao(1)
                .dataHoraSubmissao(LocalDateTime.now())
                .codigoFonte(CODIGO_FONTE)
                .estadoSubmissao(estado)
                .build());
    }

    String estadoAtual(Long submissaoId) {
        return submissaoRepository.findById(submissaoId).orElseThrow().getEstadoSubmissao().getCodigo();
    }

    List<String> transicoes(Long submissaoId) {
        return historicoRepository.findBySubmissaoIdOrderByDataHoraAscIdAsc(submissaoId).stream()
                .map(registro -> registro.getEstadoAnterior() + "->" + registro.getEstadoNovo())
                .toList();
    }

    @Test
    @DisplayName("Avaliação bem-sucedida conclui a submissão com nota, justificativa e evidências")
    void testAvaliacaoComSucesso(CapturedOutput output) {
        when(clienteLlm.enviarPrompt(any(), anyString())).thenReturn(RESPOSTA_VALIDA);
        Submissao submissao = salvarSubmissao(new EmFila());

        avaliacaoService.avaliarSubmissao(submissao.getId());

        assertEquals("CONCLUIDA", estadoAtual(submissao.getId()));
        Avaliacao avaliacao = avaliacaoRepository.findBySubmissaoId(submissao.getId()).orElseThrow();
        assertEquals(8.5, avaliacao.getNota());
        assertEquals("Classes coesas e baixo acoplamento", avaliacao.getJustificativa());
        assertEquals(List.of("Arena depende da interface Jogador", "Nenhum método longo"),
                avaliacao.getEvidencias());

        List<ResultadoModelo> resultados = resultadoModeloRepository.findBySubmissaoIdOrderByIdAsc(submissao.getId());
        assertEquals(1, resultados.size());
        assertTrue(resultados.getFirst().isSucesso());
        assertEquals("grok-2", resultados.getFirst().getModelo());
        assertEquals(8.5, resultados.getFirst().getNota());
        assertNull(resultados.getFirst().getErro());

        assertEquals(List.of("EMFILA->PROCESSANDO", "PROCESSANDO->CONCLUIDA"), transicoes(submissao.getId()));
        String prefixo = "Notificação ao estudante " + estudante.getId() + ": submissão " + submissao.getId();
        assertTrue(output.getOut().contains(prefixo + " - EMFILA -> PROCESSANDO"));
        assertTrue(output.getOut().contains(prefixo + " - PROCESSANDO -> CONCLUIDA"));
        assertTrue(output.getOut().contains("Notificação ao estudante " + estudante.getId()
                + ": resultado da avaliação disponível para a submissão " + submissao.getId()));
    }

    @Test
    @DisplayName("Avaliação usa o LLM padrão da atividade e um prompt com problema, estratégia e código")
    void testAvaliacaoUsaLlmPadraoEPromptCompleto() {
        when(clienteLlm.enviarPrompt(any(), anyString())).thenReturn(RESPOSTA_VALIDA);
        Submissao submissao = salvarSubmissao(new EmFila());

        avaliacaoService.avaliarSubmissao(submissao.getId());

        ArgumentCaptor<ConfiguracaoLlm> configuracao = ArgumentCaptor.forClass(ConfiguracaoLlm.class);
        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(clienteLlm).enviarPrompt(configuracao.capture(), prompt.capture());

        assertEquals("grok-2", configuracao.getValue().getModelo());
        assertEquals("https://api.x.ai/v1", configuracao.getValue().getUrlApi());
        assertEquals("chave-real-da-api", configuracao.getValue().getChaveApi());

        for (String trecho : List.of(
                "Verifique se as classes possuem alta coesão e baixo acoplamento.",
                "Arena de Batalha",
                "Modele uma arena onde jogadores se enfrentam",
                "- Cadastrar jogador",
                "- Iniciar batalha",
                "- Usar o padrão Strategy",
                CODIGO_FONTE,
                "sem executá-la")) {
            assertTrue(prompt.getValue().contains(trecho), trecho);
        }
    }

    @Test
    @DisplayName("Erro na API do modelo resulta em Falha e registra o erro do modelo")
    void testFalhaNaApiDoModelo(CapturedOutput output) {
        when(clienteLlm.enviarPrompt(any(), anyString()))
                .thenThrow(new FalhaAvaliacaoException("Falha na comunicação com o modelo grok-2: 500"));
        Submissao submissao = salvarSubmissao(new EmFila());

        avaliacaoService.avaliarSubmissao(submissao.getId());

        assertEquals("FALHA", estadoAtual(submissao.getId()));
        assertTrue(avaliacaoRepository.findBySubmissaoId(submissao.getId()).isEmpty());
        List<ResultadoModelo> resultados = resultadoModeloRepository.findBySubmissaoIdOrderByIdAsc(submissao.getId());
        assertEquals(1, resultados.size());
        assertFalse(resultados.getFirst().isSucesso());
        assertEquals("Falha na comunicação com o modelo grok-2: 500", resultados.getFirst().getErro());
        assertNull(resultados.getFirst().getNota());
        assertEquals(List.of("EMFILA->PROCESSANDO", "PROCESSANDO->FALHA"), transicoes(submissao.getId()));
        assertTrue(output.getOut().contains("Notificação ao estudante " + estudante.getId()
                + ": resultado da avaliação (falha) disponível para a submissão " + submissao.getId()));
    }

    @Test
    @DisplayName("Resposta inválida do modelo resulta em Falha")
    void testRespostaInvalidaDoModelo() {
        when(clienteLlm.enviarPrompt(any(), anyString()))
                .thenReturn("{\"nota\": 15, \"justificativa\": \"J\", \"evidencias\": [\"E\"]}");
        Submissao submissao = salvarSubmissao(new EmFila());

        avaliacaoService.avaliarSubmissao(submissao.getId());

        assertEquals("FALHA", estadoAtual(submissao.getId()));
        assertTrue(avaliacaoRepository.findBySubmissaoId(submissao.getId()).isEmpty());
        assertEquals("Resposta do modelo sem nota válida entre 0 e 10",
                resultadoModeloRepository.findBySubmissaoIdOrderByIdAsc(submissao.getId()).getFirst().getErro());
    }

    @Test
    @DisplayName("Erro inesperado registra ID e exceção completa e altera a submissão para Falha")
    void testErroInesperadoResultaEmFalha(CapturedOutput output) {
        when(clienteLlm.enviarPrompt(any(), anyString())).thenThrow(
                new IllegalStateException("erro inesperado", new IllegalArgumentException("causa original")));
        Submissao submissao = salvarSubmissao(new EmFila());

        avaliacaoService.avaliarSubmissao(submissao.getId());

        assertEquals("FALHA", estadoAtual(submissao.getId()));
        assertTrue(avaliacaoRepository.findBySubmissaoId(submissao.getId()).isEmpty());
        assertEquals(List.of("EMFILA->PROCESSANDO", "PROCESSANDO->FALHA"), transicoes(submissao.getId()));
        assertTrue(output.getAll().contains("Falha inesperada ao avaliar a submissão " + submissao.getId()));
        assertTrue(output.getAll().contains("java.lang.IllegalStateException: erro inesperado"));
        assertTrue(output.getAll().contains("Caused by: java.lang.IllegalArgumentException: causa original"));
    }

    @Test
    @DisplayName("Somente submissões em fila podem ser avaliadas")
    void testSubmissaoForaDaFilaNaoEhAvaliada() {
        Submissao submissao = salvarSubmissao(new Processando());

        assertThrows(RegraNegocioException.class, () -> avaliacaoService.avaliarSubmissao(submissao.getId()));

        assertEquals("PROCESSANDO", estadoAtual(submissao.getId()));
        assertTrue(transicoes(submissao.getId()).isEmpty());
        verify(clienteLlm, never()).enviarPrompt(any(), anyString());
    }

    @Test
    @DisplayName("Submissão inexistente não é avaliada")
    void testSubmissaoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> avaliacaoService.avaliarSubmissao(999L));

        verify(clienteLlm, never()).enviarPrompt(any(), anyString());
    }

    @Test
    @DisplayName("Uma submissão concluída não é avaliada novamente")
    void testSubmissaoConcluidaNaoEhReavaliada() {
        when(clienteLlm.enviarPrompt(any(), anyString())).thenReturn(RESPOSTA_VALIDA);
        Submissao submissao = salvarSubmissao(new EmFila());
        avaliacaoService.avaliarSubmissao(submissao.getId());

        assertThrows(RegraNegocioException.class, () -> avaliacaoService.avaliarSubmissao(submissao.getId()));

        assertEquals(1, avaliacaoRepository.count());
        assertEquals(1, resultadoModeloRepository.count());
        assertEquals(2, transicoes(submissao.getId()).size());
    }
}
