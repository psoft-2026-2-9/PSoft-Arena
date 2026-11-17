package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeAberta;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeConcluida;
import br.edu.ufcg.psoftarena.Model.State.Atividade.EstadoAtividade;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.EstrategiaRepository;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Classe de testes de consulta de atividades")
public class AtividadeConsultaControllerTests {

    final String URI_ATIVIDADE = "/atividades";

    @Autowired
    MockMvc driver;

    @Autowired
    AtividadeRepository atividadeRepository;

    @Autowired
    TurmaRepository turmaRepository;

    @Autowired
    EstudanteRepository estudanteRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    ProblemaRepository problemaRepository;

    @Autowired
    EstrategiaRepository estrategiaRepository;

    @Autowired
    TextEncryptor textEncryptor;

    Professor salvarProfessor(String nome) {
        return professorRepository.save(Professor.builder().nome(nome).build());
    }

    Turma salvarTurma(String nome, String codigo, StatusTurma status, Professor professor) {
        return turmaRepository.save(Turma.builder()
                .nome(nome)
                .codigo(codigo)
                .periodoLetivo("2026.2")
                .professor(professor)
                .status(status)
                .build());
    }

    Estudante salvarEstudante(String nome, String matricula, String email) {
        return estudanteRepository.save(Estudante.builder()
                .nome(nome)
                .matricula(matricula)
                .email(email)
                .build());
    }

    Problema salvarProblema(String titulo) {
        return problemaRepository.save(Problema.builder()
                .titulo(titulo)
                .descricao("Descrição do problema")
                .requisitosFuncionais(List.of("Cadastrar jogador"))
                .requisitosDeProjeto(List.of("Usar Spring"))
                .status(StatusProblema.ATIVO)
                .build());
    }

    Estrategia salvarEstrategia(String nome) {
        return estrategiaRepository.save(Estrategia.builder()
                .nome(nome)
                .tipo(TipoEstrategia.CODE_SMELLS)
                .descricao("Avalia a presença de code smells")
                .prompt("Analise o código e identifique code smells.")
                .build());
    }

    Atividade salvarAtividade(Turma turma, Problema problema, Estrategia estrategia,
                              String titulo, EstadoAtividade estado) {
        return atividadeRepository.save(Atividade.builder()
                .titulo(titulo)
                .descricao("Descrição da " + titulo)
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

    Turma associarEstudante(Turma turma, Estudante estudante) {
        turma.getEstudantes().add(estudante);
        return turmaRepository.saveAndFlush(turma);
    }

    Turma removerEstudante(Turma turma, Estudante estudante) {
        turma.getEstudantes().remove(estudante);
        return turmaRepository.saveAndFlush(turma);
    }

    @Test
    @DisplayName("Lista apenas atividades das turmas do estudante")
    void deveListarSomenteAtividadesDasTurmasDoEstudante() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        Turma turmaA = associarEstudante(
                salvarTurma("Turma A", "TURMA-A", StatusTurma.ATIVA, professor), estudante);
        Turma turmaB = associarEstudante(
                salvarTurma("Turma B", "TURMA-B", StatusTurma.ATIVA, professor), estudante);
        Turma turmaC = salvarTurma("Turma C", "TURMA-C", StatusTurma.ATIVA, professor);

        Problema problema = salvarProblema("Arena");
        Estrategia estrategia = salvarEstrategia("Code Smells Básico");

        salvarAtividade(turmaA, problema, estrategia, "Atividade A", new AtividadeAberta());
        salvarAtividade(turmaB, problema, estrategia, "Atividade B", new AtividadeAberta());
        salvarAtividade(turmaC, problema, estrategia, "Atividade C", new AtividadeAberta());

        driver.perform(get(URI_ATIVIDADE + "/estudante/" + estudante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.titulo == 'Atividade A')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.titulo == 'Atividade B')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.titulo == 'Atividade C')]").isEmpty())
                .andDo(print());
    }

    @Test
    @DisplayName("Retorna lista vazia para estudante sem turmas")
    void deveRetornarListaVaziaQuandoEstudanteNaoTemTurmas() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        Turma turma = salvarTurma("Turma A", "TURMA-A", StatusTurma.ATIVA, professor);

        Problema problema = salvarProblema("Arena");
        Estrategia estrategia = salvarEstrategia("Code Smells Básico");
        salvarAtividade(turma, problema, estrategia, "Atividade A", new AtividadeAberta());

        driver.perform(get(URI_ATIVIDADE + "/estudante/" + estudante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("Retorna 404 para estudante inexistente na listagem")
    void deveRetornarNotFoundQuandoEstudanteNaoExisteNaListagem() throws Exception {
        driver.perform(get(URI_ATIVIDADE + "/estudante/999"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Retorna detalhe completo da atividade do estudante")
    void deveRetornarDetalheCompletoDaAtividade() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        Turma turma = associarEstudante(
                salvarTurma("Turma A", "TURMA-A", StatusTurma.ATIVA, professor), estudante);

        Problema problema = salvarProblema("Arena");
        Estrategia estrategia = salvarEstrategia("Code Smells Básico");
        Atividade atividade = salvarAtividade(turma, problema, estrategia, "Atividade 1", new AtividadeAberta());

        driver.perform(get(URI_ATIVIDADE + "/" + atividade.getId() + "/estudante/" + estudante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(atividade.getId()))
                .andExpect(jsonPath("$.titulo").value("Atividade 1"))
                .andExpect(jsonPath("$.turma.id").value(turma.getId()))
                .andExpect(jsonPath("$.turma.nome").value("Turma A"))
                .andExpect(jsonPath("$.turma.codigo").value("TURMA-A"))
                .andExpect(jsonPath("$.turma.periodo_letivo").value("2026.2"))
                .andExpect(jsonPath("$.turma.status").value("ATIVA"))
                .andExpect(jsonPath("$.problema.id").value(problema.getId()))
                .andExpect(jsonPath("$.problema.titulo").value("Arena"))
                .andExpect(jsonPath("$.problema.descricao").value("Descrição do problema"))
                .andExpect(jsonPath("$.problema.requisitos_funcionais[0]").value("Cadastrar jogador"))
                .andExpect(jsonPath("$.problema.requisitos_de_projeto[0]").value("Usar Spring"))
                .andExpect(jsonPath("$.descricao").value("Descrição da Atividade 1"))
                .andExpect(jsonPath("$.estrategia_avaliacao.id").value(estrategia.getId()))
                .andExpect(jsonPath("$.estrategia_avaliacao.nome").value("Code Smells Básico"))
                .andExpect(jsonPath("$.estrategia_avaliacao.tipo").value("CODE_SMELLS"))
                .andExpect(jsonPath("$.estrategia_avaliacao.descricao").value("Avalia a presença de code smells"))
                .andExpect(jsonPath("$.prazo").exists())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.llm_modelo").doesNotExist())
                .andExpect(jsonPath("$.llm_url_api").doesNotExist())
                .andExpect(jsonPath("$.llm_chave_api").doesNotExist())
                .andExpect(jsonPath("$.estrategia_avaliacao.prompt").doesNotExist())
                .andDo(print());
    }

    @Test
    @DisplayName("Retorna 404 para atividade inexistente")
    void deveRetornarNotFoundQuandoAtividadeNaoExiste() throws Exception {
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        driver.perform(get(URI_ATIVIDADE + "/999/estudante/" + estudante.getId()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Retorna 404 para estudante inexistente no detalhe")
    void deveRetornarNotFoundQuandoEstudanteNaoExisteNoDetalhe() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", StatusTurma.ATIVA, professor);
        Problema problema = salvarProblema("Arena");
        Estrategia estrategia = salvarEstrategia("Code Smells Básico");
        Atividade atividade = salvarAtividade(turma, problema, estrategia, "Atividade 1", new AtividadeAberta());

        driver.perform(get(URI_ATIVIDADE + "/" + atividade.getId() + "/estudante/999"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Retorna 403 quando estudante não participa da turma da atividade")
    void deveRetornarForbiddenQuandoEstudanteNaoParticipaDaTurma() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        Turma turmaA = salvarTurma("Turma A", "TURMA-A", StatusTurma.ATIVA, professor);
        associarEstudante(salvarTurma("Turma B", "TURMA-B", StatusTurma.ATIVA, professor), estudante);

        Problema problema = salvarProblema("Arena");
        Estrategia estrategia = salvarEstrategia("Code Smells Básico");
        Atividade atividade = salvarAtividade(turmaA, problema, estrategia, "Atividade A", new AtividadeAberta());

        driver.perform(get(URI_ATIVIDADE + "/" + atividade.getId() + "/estudante/" + estudante.getId()))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("Mantém atividade concluída consultável")
    void deveManterAtividadeConcluidaConsultavel() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        Turma turma = associarEstudante(
                salvarTurma("Turma A", "TURMA-A", StatusTurma.ATIVA, professor), estudante);

        Problema problema = salvarProblema("Arena");
        Estrategia estrategia = salvarEstrategia("Code Smells Básico");
        Atividade atividade = salvarAtividade(turma, problema, estrategia, "Atividade Concluída",
                new AtividadeConcluida());

        driver.perform(get(URI_ATIVIDADE + "/estudante/" + estudante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Atividade Concluída"))
                .andExpect(jsonPath("$[0].status").value("CONCLUIDA"))
                .andDo(print());

        driver.perform(get(URI_ATIVIDADE + "/" + atividade.getId() + "/estudante/" + estudante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(atividade.getId()))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andDo(print());
    }

    @Test
    @DisplayName("Mantém atividade de turma encerrada consultável")
    void deveManterAtividadeDeTurmaEncerradaConsultavel() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        Turma turma = salvarTurma("Turma Encerrada", "TURMA-ENC", StatusTurma.ENCERRADA, professor);
        associarEstudante(turma, estudante);

        Problema problema = salvarProblema("Arena");
        Estrategia estrategia = salvarEstrategia("Code Smells Básico");
        Atividade atividade = salvarAtividade(turma, problema, estrategia, "Atividade Encerrada",
                new AtividadeAberta());

        driver.perform(get(URI_ATIVIDADE + "/estudante/" + estudante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Atividade Encerrada"))
                .andExpect(jsonPath("$[0].turma.status").value("ENCERRADA"))
                .andDo(print());

        driver.perform(get(URI_ATIVIDADE + "/" + atividade.getId() + "/estudante/" + estudante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(atividade.getId()))
                .andDo(print());
    }

    @Test
    @DisplayName("Remove acesso após estudante ser desassociado da turma")
    void deveRemoverAcessoAposEstudanteSerRemovidoDaTurma() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        Turma turma = associarEstudante(
                salvarTurma("Turma A", "TURMA-A", StatusTurma.ATIVA, professor), estudante);

        Problema problema = salvarProblema("Arena");
        Estrategia estrategia = salvarEstrategia("Code Smells Básico");
        Atividade atividade = salvarAtividade(turma, problema, estrategia, "Atividade A", new AtividadeAberta());

        removerEstudante(turma, estudante);

        driver.perform(get(URI_ATIVIDADE + "/estudante/" + estudante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andDo(print());

        driver.perform(get(URI_ATIVIDADE + "/" + atividade.getId() + "/estudante/" + estudante.getId()))
                .andExpect(status().isForbidden())
                .andDo(print());
    }
}
