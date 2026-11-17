package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.DTO.SubmissaoPostDto;
import br.edu.ufcg.psoftarena.DTO.SubmissaoResponseDto;
import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.ENUM.StatusSubmissao;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Model.State.AtividadeAberta;
import br.edu.ufcg.psoftarena.Model.State.AtividadeConcluida;
import br.edu.ufcg.psoftarena.Model.State.EstadoAtividade;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.EstrategiaRepository;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Classe de testes de submissão")
public class SubmissaoControllerTests {

    final String CODIGO_FONTE = "public class Main { public static void main(String[] args) {} }";

    final AtomicLong matriculaCounter = new AtomicLong();

    @Autowired
    MockMvc driver;

    @Autowired
    SubmissaoRepository submissaoRepository;

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

    ObjectMapper objectMapper = new ObjectMapper();

    Professor professor;
    Turma turma;
    Estudante estudante;
    Atividade atividade;

    @BeforeEach
    void setUp() {
        professor = salvarProfessor();
        turma = salvarTurma(StatusTurma.ATIVA);
        estudante = salvarEstudante();
        matricularEstudanteNaTurma(estudante);
        atividade = salvarAtividade(turma, new AtividadeAberta(), LocalDateTime.now().plusDays(7));
    }

    @AfterEach
    void tearDown() {
        submissaoRepository.deleteAll();
        atividadeRepository.deleteAll();
        turmaRepository.deleteAll();
        estudanteRepository.deleteAll();
        problemaRepository.deleteAll();
        estrategiaRepository.deleteAll();
        professorRepository.deleteAll();
    }

    Professor salvarProfessor() {
        return professorRepository.save(Professor.builder().nome("Professor A").build());
    }

    Turma salvarTurma(StatusTurma status) {
        return turmaRepository.save(Turma.builder()
                .nome("Turma A")
                .codigo("TURMA-A-" + matriculaCounter.incrementAndGet())
                .periodoLetivo("2026.2")
                .professor(professor)
                .status(status)
                .build());
    }

    Estudante salvarEstudante() {
        long contador = matriculaCounter.incrementAndGet();
        return estudanteRepository.save(Estudante.builder()
                .nome("Estudante " + contador)
                .matricula("2026" + contador)
                .email("estudante" + contador + "@ufcg.edu.br")
                .build());
    }

    void matricularEstudanteNaTurma(Estudante estudanteParaMatricular) {
        turma.getEstudantes().add(estudanteParaMatricular);
        turmaRepository.save(turma);
    }

    Problema salvarProblema(StatusProblema status) {
        return problemaRepository.save(Problema.builder()
                .titulo("Arena")
                .descricao("Descrição do problema")
                .requisitosFuncionais(List.of("Cadastrar jogador"))
                .requisitosDeProjeto(List.of("Usar Spring"))
                .status(status)
                .build());
    }

    Estrategia salvarEstrategia() {
        return estrategiaRepository.save(Estrategia.builder()
                .nome("Code Smells Básico")
                .tipo(TipoEstrategia.CODE_SMELLS)
                .descricao("Avalia a presença de code smells")
                .prompt("Analise o código e identifique code smells.")
                .build());
    }

    Atividade salvarAtividade(Turma turmaDaAtividade, EstadoAtividade estado, LocalDateTime prazo) {
        return atividadeRepository.save(Atividade.builder()
                .titulo("Atividade 1")
                .descricao("Implemente o problema X")
                .prazo(prazo)
                .status(estado)
                .turma(turmaDaAtividade)
                .problema(salvarProblema(StatusProblema.ATIVO))
                .estrategia(salvarEstrategia())
                .llmModelo("grok-2")
                .llmUrlApi("https://api.x.ai/v1")
                .llmChaveApiCriptografada(textEncryptor.encrypt("sk-test"))
                .build());
    }

    String corpoSubmissao(Long estudanteId, String codigoFonte) throws Exception {
        return objectMapper.writeValueAsString(SubmissaoPostDto.builder()
                .estudanteId(estudanteId)
                .codigoFonte(codigoFonte)
                .build());
    }

    String uriSubmissao(Long atividadeId) {
        return "/atividades/" + atividadeId + "/submissoes";
    }

    SubmissaoResponseDto submeterComSucesso(Long atividadeId, Long estudanteId) throws Exception {
        String responseJSONString = driver.perform(post(uriSubmissao(atividadeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoSubmissao(estudanteId, CODIGO_FONTE)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(responseJSONString, SubmissaoResponseDto.class);
    }

    @Test
    @DisplayName("Estudante da turma submete solução para atividade aberta")
    void testQuandoEstudanteDaTurmaSubmeteSolucao() throws Exception {
        String responseJSONString = driver.perform(post(uriSubmissao(atividade.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoSubmissao(estudante.getId(), CODIGO_FONTE)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        SubmissaoResponseDto resultado = objectMapper.readValue(responseJSONString, SubmissaoResponseDto.class);

        assertEquals(estudante.getId(), resultado.getEstudanteId());
        assertEquals(atividade.getId(), resultado.getAtividadeId());
        assertEquals(1, resultado.getVersao());
        assertEquals(StatusSubmissao.SUBMETIDA, resultado.getStatus());
        assertNotNull(resultado.getDataHoraSubmissao());
        assertEquals(1, submissaoRepository.findAll().size());
    }

    @Test
    @DisplayName("Reenvio do mesmo estudante registra nova versão")
    void testQuandoReenvioRegistraNovaVersao() throws Exception {
        SubmissaoResponseDto primeira = submeterComSucesso(atividade.getId(), estudante.getId());
        SubmissaoResponseDto segunda = submeterComSucesso(atividade.getId(), estudante.getId());

        assertEquals(1, primeira.getVersao());
        assertEquals(2, segunda.getVersao());
        assertNotEquals(primeira.getId(), segunda.getId());
        assertEquals(2, submissaoRepository.findAll().size());
    }

    @Test
    @DisplayName("Estudante de outra turma não pode submeter")
    void testQuandoEstudanteForaDaTurmaSubmete() throws Exception {
        Estudante estudanteFora = salvarEstudante();

        driver.perform(post(uriSubmissao(atividade.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoSubmissao(estudanteFora.getId(), CODIGO_FONTE)))
                .andExpect(status().isConflict())
                .andDo(print());

        assertEquals(0, submissaoRepository.findAll().size());
    }

    @Test
    @DisplayName("Atividade concluída não aceita submissões")
    void testQuandoAtividadeConcluida() throws Exception {
        Atividade atividadeConcluida = salvarAtividade(turma, new AtividadeConcluida(),
                LocalDateTime.now().plusDays(7));

        driver.perform(post(uriSubmissao(atividadeConcluida.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoSubmissao(estudante.getId(), CODIGO_FONTE)))
                .andExpect(status().isConflict())
                .andDo(print());

        assertEquals(0, submissaoRepository.findAll().size());
    }

    @Test
    @DisplayName("Prazo expirado não aceita submissões")
    void testQuandoPrazoExpirado() throws Exception {
        Atividade atividadeAtrasada = salvarAtividade(turma, new AtividadeAberta(),
                LocalDateTime.now().minusDays(1));

        driver.perform(post(uriSubmissao(atividadeAtrasada.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoSubmissao(estudante.getId(), CODIGO_FONTE)))
                .andExpect(status().isConflict())
                .andDo(print());

        assertEquals(0, submissaoRepository.findAll().size());
    }

    @Test
    @DisplayName("Atividade inexistente retorna 404")
    void testQuandoAtividadeInexistente() throws Exception {
        driver.perform(post(uriSubmissao(999L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoSubmissao(estudante.getId(), CODIGO_FONTE)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Estudante inexistente retorna 404")
    void testQuandoEstudanteInexistente() throws Exception {
        driver.perform(post(uriSubmissao(atividade.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoSubmissao(999L, CODIGO_FONTE)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Código-fonte em branco retorna 400")
    void testQuandoCodigoFonteEmBranco() throws Exception {
        driver.perform(post(uriSubmissao(atividade.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoSubmissao(estudante.getId(), "")))
                .andExpect(status().isBadRequest())
                .andDo(print());

        assertEquals(0, submissaoRepository.findAll().size());
    }

    @Test
    @DisplayName("Estudante com submissões registradas não pode ser removido")
    void testQuandoRemovemosEstudanteComSubmissoes() throws Exception {
        submeterComSucesso(atividade.getId(), estudante.getId());

        turma.getEstudantes().remove(estudante);
        turmaRepository.save(turma);

        driver.perform(delete("/v1/estudantes/" + estudante.getId()))
                .andExpect(status().isConflict())
                .andDo(print());

        assertEquals(1, submissaoRepository.findAll().size());
    }

    @Test
    @DisplayName("Estudantes diferentes na mesma atividade iniciam na versão 1")
    void testQuandoEstudantesDiferentesSubmetem() throws Exception {
        Estudante estudanteB = salvarEstudante();
        matricularEstudanteNaTurma(estudanteB);

        SubmissaoResponseDto primeira = submeterComSucesso(atividade.getId(), estudante.getId());
        SubmissaoResponseDto segunda = submeterComSucesso(atividade.getId(), estudanteB.getId());

        assertEquals(1, primeira.getVersao());
        assertEquals(1, segunda.getVersao());
        assertEquals(2, submissaoRepository.findAll().size());
    }
}
