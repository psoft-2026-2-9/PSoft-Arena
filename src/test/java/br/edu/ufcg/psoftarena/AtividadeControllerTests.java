package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeAberta;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeConcluida;
import br.edu.ufcg.psoftarena.Model.State.Atividade.EstadoAtividade;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.EstrategiaRepository;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Classe de testes de atividade")
public class AtividadeControllerTests {

    final String URI_ATIVIDADE = "/atividades";

    @Autowired
    MockMvc driver;

    @Autowired
    AtividadeRepository atividadeRepository;

    @Autowired
    TurmaRepository turmaRepository;

    @Autowired
    ProblemaRepository problemaRepository;

    @Autowired
    EstrategiaRepository estrategiaRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    TextEncryptor textEncryptor;

    Professor salvarProfessor() {
        return professorRepository.save(Professor.builder().nome("Professor A").build());
    }

    Turma salvarTurma(Professor professor, StatusTurma status) {
        return turmaRepository.save(Turma.builder()
                .nome("Turma A")
                .codigo("TURMA-A")
                .periodoLetivo("2026.2")
                .professor(professor)
                .status(status)
                .build());
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

    Atividade salvarAtividade(Turma turma, Problema problema, Estrategia estrategia, EstadoAtividade estado) {
        return atividadeRepository.save(Atividade.builder()
                .titulo("Atividade 1")
                .descricao("Implemente o problema X")
                .prazo(LocalDateTime.now().plusDays(7))
                .status(estado)
                .turma(turma)
                .problema(problema)
                .estrategia(estrategia)
                .llmModelo("grok-2")
                .llmUrlApi("https://api.x.ai/v1")
                .llmChaveApiCriptografada(textEncryptor.encrypt("sk-test"))
                .build());
    }

    String jsonAtividade(Long turmaId, Long problemaId, Long estrategiaId) {
        return jsonAtividade(turmaId, problemaId, estrategiaId, LocalDateTime.now().plusDays(7));
    }

    String jsonAtividade(Long turmaId, Long problemaId, Long estrategiaId, LocalDateTime prazo) {
        return """
                {
                    "titulo": "Atividade 1",
                    "descricao": "Implemente o problema X",
                    "prazo": "%s",
                    "turma_id": %d,
                    "problema_id": %d,
                    "estrategia_id": %d,
                    "llm_modelo": "grok-2",
                    "llm_url_api": "https://api.x.ai/v1",
                    "llm_chave_api": "sk-test"
                }
                """.formatted(prazo, turmaId, problemaId, estrategiaId);
    }

    @Test
    @DisplayName("Cria atividade aberta e não retorna a chave da LLM")
    void deveCriarAtividade() throws Exception {
        Professor professor = salvarProfessor();
        Turma turma = salvarTurma(professor, StatusTurma.ATIVA);
        Problema problema = salvarProblema(StatusProblema.ATIVO);
        Estrategia estrategia = salvarEstrategia();

        driver.perform(post(URI_ATIVIDADE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtividade(turma.getId(), problema.getId(), estrategia.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Atividade 1"))
                .andExpect(jsonPath("$.descricao").value("Implemente o problema X"))
                .andExpect(jsonPath("$.prazo").exists())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.turma_id").value(turma.getId()))
                .andExpect(jsonPath("$.problema_id").value(problema.getId()))
                .andExpect(jsonPath("$.estrategia_id").value(estrategia.getId()))
                .andExpect(jsonPath("$.llm_modelo").value("grok-2"))
                .andExpect(jsonPath("$.llm_url_api").value("https://api.x.ai/v1"))
                .andExpect(jsonPath("$.llm_chave_api").doesNotExist())
                .andDo(print());

        assertEquals(1, atividadeRepository.count());
        String chaveCriptografada = atividadeRepository.findAll().get(0).getLlmChaveApiCriptografada();
        assertNotEquals("sk-test", chaveCriptografada);
        assertEquals("sk-test", textEncryptor.decrypt(chaveCriptografada));
    }

    @Test
    @DisplayName("Rejeita atividade com campos obrigatórios ausentes")
    void deveRejeitarAtividadeComCamposObrigatoriosAusentes() throws Exception {
        driver.perform(post(URI_ATIVIDADE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andDo(print());

        assertEquals(0, atividadeRepository.count());
    }

    @Test
    @DisplayName("Rejeita atividade com prazo no passado")
    void deveRejeitarAtividadeComPrazoNoPassado() throws Exception {
        Professor professor = salvarProfessor();
        Turma turma = salvarTurma(professor, StatusTurma.ATIVA);
        Problema problema = salvarProblema(StatusProblema.ATIVO);
        Estrategia estrategia = salvarEstrategia();

        driver.perform(post(URI_ATIVIDADE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtividade(turma.getId(), problema.getId(), estrategia.getId(),
                                LocalDateTime.now().minusDays(1))))
                .andExpect(status().isBadRequest())
                .andDo(print());

        assertEquals(0, atividadeRepository.count());
    }

    @Test
    @DisplayName("Retorna 404 quando a turma não existe")
    void deveRetornarNotFoundQuandoTurmaNaoExiste() throws Exception {
        driver.perform(post(URI_ATIVIDADE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtividade(999L, 999L, 999L)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Turma não encontrada"))
                .andDo(print());

        assertEquals(0, atividadeRepository.count());
    }

    @Test
    @DisplayName("Retorna 404 quando o problema não existe")
    void deveRetornarNotFoundQuandoProblemaNaoExiste() throws Exception {
        Professor professor = salvarProfessor();
        Turma turma = salvarTurma(professor, StatusTurma.ATIVA);

        driver.perform(post(URI_ATIVIDADE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtividade(turma.getId(), 999L, 999L)))
                .andExpect(status().isNotFound())
                .andDo(print());

        assertEquals(0, atividadeRepository.count());
    }

    @Test
    @DisplayName("Retorna 404 quando a estratégia não existe")
    void deveRetornarNotFoundQuandoEstrategiaNaoExiste() throws Exception {
        Professor professor = salvarProfessor();
        Turma turma = salvarTurma(professor, StatusTurma.ATIVA);
        Problema problema = salvarProblema(StatusProblema.ATIVO);

        driver.perform(post(URI_ATIVIDADE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtividade(turma.getId(), problema.getId(), 999L)))
                .andExpect(status().isNotFound())
                .andDo(print());

        assertEquals(0, atividadeRepository.count());
    }

    @Test
    @DisplayName("Retorna 409 quando a turma está encerrada")
    void deveRetornarConflictQuandoTurmaEstaEncerrada() throws Exception {
        Professor professor = salvarProfessor();
        Turma turma = salvarTurma(professor, StatusTurma.ENCERRADA);
        Problema problema = salvarProblema(StatusProblema.ATIVO);
        Estrategia estrategia = salvarEstrategia();

        driver.perform(post(URI_ATIVIDADE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtividade(turma.getId(), problema.getId(), estrategia.getId())))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Turma encerrada não pode receber novas atividades"))
                .andDo(print());

        assertEquals(0, atividadeRepository.count());
    }

    @Test
    @DisplayName("Retorna 409 quando o problema está inativo")
    void deveRetornarConflictQuandoProblemaEstaInativo() throws Exception {
        Professor professor = salvarProfessor();
        Turma turma = salvarTurma(professor, StatusTurma.ATIVA);
        Problema problema = salvarProblema(StatusProblema.INATIVO);
        Estrategia estrategia = salvarEstrategia();

        driver.perform(post(URI_ATIVIDADE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtividade(turma.getId(), problema.getId(), estrategia.getId())))
                .andExpect(status().isConflict())
                .andDo(print());

        assertEquals(0, atividadeRepository.count());
    }

    @Test
    @DisplayName("Conclui uma atividade aberta")
    void deveConcluirAtividade() throws Exception {
        Professor professor = salvarProfessor();
        Turma turma = salvarTurma(professor, StatusTurma.ATIVA);
        Problema problema = salvarProblema(StatusProblema.ATIVO);
        Estrategia estrategia = salvarEstrategia();
        Atividade atividade = salvarAtividade(turma, problema, estrategia, new AtividadeAberta());

        driver.perform(put(URI_ATIVIDADE + "/" + atividade.getId() + "/concluir")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(atividade.getId()))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andDo(print());

        assertEquals("CONCLUIDA",
                atividadeRepository.findById(atividade.getId()).orElseThrow().getStatus().getCodigo());
    }

    @Test
    @DisplayName("Retorna 404 ao concluir atividade inexistente")
    void deveRetornarNotFoundAoConcluirAtividadeInexistente() throws Exception {
        driver.perform(put(URI_ATIVIDADE + "/999/concluir")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Retorna 409 ao concluir atividade já concluída")
    void deveRetornarConflictAoConcluirAtividadeJaConcluida() throws Exception {
        Professor professor = salvarProfessor();
        Turma turma = salvarTurma(professor, StatusTurma.ATIVA);
        Problema problema = salvarProblema(StatusProblema.ATIVO);
        Estrategia estrategia = salvarEstrategia();
        Atividade atividade = salvarAtividade(turma, problema, estrategia, new AtividadeConcluida());

        driver.perform(put(URI_ATIVIDADE + "/" + atividade.getId() + "/concluir")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("Retorna 409 ao excluir problema associado a uma atividade")
    void deveRetornarConflictAoExcluirProblemaAssociadoAtividade() throws Exception {
        Professor professor = salvarProfessor();
        Turma turma = salvarTurma(professor, StatusTurma.ATIVA);
        Problema problema = salvarProblema(StatusProblema.ATIVO);
        Estrategia estrategia = salvarEstrategia();
        Atividade atividade = salvarAtividade(turma, problema, estrategia, new AtividadeAberta());

        driver.perform(delete("/problemas/" + problema.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(print());

        assertTrue(problemaRepository.existsById(problema.getId()));
        assertTrue(atividadeRepository.existsById(atividade.getId()));
    }
}
