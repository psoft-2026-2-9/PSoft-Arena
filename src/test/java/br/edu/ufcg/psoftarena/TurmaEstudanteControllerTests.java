package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.DTO.EstudanteResponseDto;
import br.edu.ufcg.psoftarena.DTO.TurmaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.TurmaResponseDto;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import br.edu.ufcg.psoftarena.Service.TurmaCrudService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Classe de testes de associação de estudantes às turmas")
public class TurmaEstudanteControllerTests {

    final String URI_TURMA = "/turmas";

    @Autowired
    MockMvc driver;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    TurmaRepository turmaRepository;

    @Autowired
    EstudanteRepository estudanteRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    TurmaCrudService turmaCrudService;

    @Autowired
    EntityManager entityManager;

    @AfterEach
    void tearDown() {
        turmaRepository.deleteAll();
        estudanteRepository.deleteAll();
        professorRepository.deleteAll();
    }

    Professor salvarProfessor(String nome) {
        return professorRepository.save(Professor.builder().nome(nome).build());
    }

    Turma salvarTurma(String nome, String codigo, String periodoLetivo, Professor professor, StatusTurma status) {
        return turmaRepository.save(Turma.builder()
                .nome(nome)
                .codigo(codigo)
                .periodoLetivo(periodoLetivo)
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

    Turma associarEstudante(Turma turma, Estudante estudante) {
        turma.getEstudantes().add(estudante);
        return turmaRepository.saveAndFlush(turma);
    }

    void adicionarViaEndpoint(Long turmaId, Long estudanteId) throws Exception {
        driver.perform(post(URI_TURMA + "/" + turmaId + "/estudantes/" + estudanteId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para adicionar um estudante a uma turma ativa")
    void testQuandoAdicionamosUmEstudanteAUmaTurmaAtiva() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        String responseJSONString = driver.perform(post(URI_TURMA + "/" + turma.getId() + "/estudantes/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertEquals(1, resultado.getEstudantes().size());
        assertEquals(estudante.getId(), resultado.getEstudantes().get(0).getId());
        assertEquals("124210001", resultado.getEstudantes().get(0).getMatricula());

        entityManager.flush();
        entityManager.clear();

        Turma turmaNoBanco = turmaRepository.findById(turma.getId()).orElseThrow();
        assertEquals(1, turmaNoBanco.getEstudantes().size());
        assertTrue(turmaNoBanco.getEstudantes().stream()
                .anyMatch(e -> e.getId().equals(estudante.getId())));
    }

    @Test
    @DisplayName("Teste para adicionar um estudante a uma turma encerrada")
    void testQuandoAdicionamosUmEstudanteAUmaTurmaEncerrada() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ENCERRADA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        driver.perform(post(URI_TURMA + "/" + turma.getId() + "/estudantes/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(print());

        entityManager.flush();
        entityManager.clear();

        assertEquals(0, turmaRepository.findById(turma.getId()).orElseThrow().getEstudantes().size());
    }

    @Test
    @DisplayName("Teste para adicionar o mesmo estudante duas vezes na mesma turma")
    void testQuandoAdicionamosOMesmoEstudanteDuasVezes() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        adicionarViaEndpoint(turma.getId(), estudante.getId());

        driver.perform(post(URI_TURMA + "/" + turma.getId() + "/estudantes/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(print());

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, turmaRepository.findById(turma.getId()).orElseThrow().getEstudantes().size());
    }

    @Test
    @DisplayName("Teste para adicionar um estudante inexistente a uma turma")
    void testQuandoAdicionamosUmEstudanteInexistente() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);

        driver.perform(post(URI_TURMA + "/" + turma.getId() + "/estudantes/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());

        entityManager.flush();
        entityManager.clear();

        assertEquals(0, turmaRepository.findById(turma.getId()).orElseThrow().getEstudantes().size());
    }

    @Test
    @DisplayName("Teste para adicionar um estudante a uma turma inexistente")
    void testQuandoAdicionamosUmEstudanteAUmaTurmaInexistente() throws Exception {
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        driver.perform(post(URI_TURMA + "/999/estudantes/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para listar os estudantes de uma turma")
    void testQuandoListamosOsEstudantesDeUmaTurma() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante maria = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        Estudante joao = salvarEstudante("João", "124210002", "joao@ccc.ufcg.edu.br");

        associarEstudante(turma, maria);
        associarEstudante(turma, joao);

        String responseJSONString = driver.perform(get(URI_TURMA + "/" + turma.getId() + "/estudantes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<EstudanteResponseDto> resultado = objectMapper.readValue(responseJSONString,
                new TypeReference<List<EstudanteResponseDto>>() {
                });

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().anyMatch(e -> e.getMatricula().equals("124210001")));
        assertTrue(resultado.stream().anyMatch(e -> e.getMatricula().equals("124210002")));
    }

    @Test
    @DisplayName("Teste para listar estudantes de uma turma inexistente")
    void testQuandoListamosEstudantesDeUmaTurmaInexistente() throws Exception {
        driver.perform(get(URI_TURMA + "/999/estudantes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para remover um estudante de uma turma")
    void testQuandoRemovemosUmEstudanteDeUmaTurma() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        associarEstudante(turma, estudante);

        driver.perform(delete(URI_TURMA + "/" + turma.getId() + "/estudantes/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print());

        entityManager.flush();
        entityManager.clear();

        assertEquals(0, turmaRepository.findById(turma.getId()).orElseThrow().getEstudantes().size());
    }

    @Test
    @DisplayName("Teste para remover uma associação inexistente")
    void testQuandoRemovemosUmaAssociacaoInexistente() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        driver.perform(delete(URI_TURMA + "/" + turma.getId() + "/estudantes/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para remover um estudante inexistente de uma turma")
    void testQuandoRemovemosUmEstudanteInexistente() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);

        driver.perform(delete(URI_TURMA + "/" + turma.getId() + "/estudantes/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para remover um estudante de uma turma encerrada")
    void testQuandoRemovemosUmEstudanteDeUmaTurmaEncerrada() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ENCERRADA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        associarEstudante(turma, estudante);

        driver.perform(delete(URI_TURMA + "/" + turma.getId() + "/estudantes/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print());

        entityManager.flush();
        entityManager.clear();

        assertEquals(0, turmaRepository.findById(turma.getId()).orElseThrow().getEstudantes().size());
    }

    @Test
    @DisplayName("Teste para associar o mesmo estudante a várias turmas")
    void testQuandoAssociamosOMesmoEstudanteAVariasTurmas() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turmaA = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Turma turmaB = salvarTurma("Turma B", "TURMA-B", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        adicionarViaEndpoint(turmaA.getId(), estudante.getId());
        adicionarViaEndpoint(turmaB.getId(), estudante.getId());

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, turmaRepository.findById(turmaA.getId()).orElseThrow().getEstudantes().size());
        assertEquals(1, turmaRepository.findById(turmaB.getId()).orElseThrow().getEstudantes().size());
    }

    @Test
    @DisplayName("Teste para associar vários estudantes à mesma turma")
    void testQuandoAssociamosVariosEstudantesAMesmaTurma() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante maria = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        Estudante joao = salvarEstudante("João", "124210002", "joao@ccc.ufcg.edu.br");

        adicionarViaEndpoint(turma.getId(), maria.getId());
        adicionarViaEndpoint(turma.getId(), joao.getId());

        entityManager.flush();
        entityManager.clear();

        assertEquals(2, turmaRepository.findById(turma.getId()).orElseThrow().getEstudantes().size());
    }

    @Test
    @DisplayName("Teste para remover estudante de uma turma sem afetar a outra")
    void testQuandoRemovemosEstudanteDeUmaTurmaSemAfetarAOutra() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turmaA = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Turma turmaB = salvarTurma("Turma B", "TURMA-B", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        associarEstudante(turmaA, estudante);
        associarEstudante(turmaB, estudante);

        driver.perform(delete(URI_TURMA + "/" + turmaA.getId() + "/estudantes/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print());

        entityManager.flush();
        entityManager.clear();

        assertEquals(0, turmaRepository.findById(turmaA.getId()).orElseThrow().getEstudantes().size());
        assertEquals(1, turmaRepository.findById(turmaB.getId()).orElseThrow().getEstudantes().size());
    }

    @Test
    @DisplayName("Teste para buscar uma turma e receber seus estudantes")
    void testQuandoBuscamosUmaTurmaElaRetornaSeusEstudantes() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        associarEstudante(turma, estudante);

        String responseJSONString = driver.perform(get(URI_TURMA + "/" + turma.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertEquals(1, resultado.getEstudantes().size());
        assertEquals(estudante.getId(), resultado.getEstudantes().get(0).getId());
        assertEquals("124210001", resultado.getEstudantes().get(0).getMatricula());
    }

    @Test
    @DisplayName("Teste para editar uma turma preservando seus estudantes")
    void testQuandoEditamosUmaTurmaPreservamosSeusEstudantes() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        associarEstudante(turma, estudante);

        TurmaPostPutDto dto = TurmaPostPutDto.builder()
                .nome("Turma A Editada")
                .codigo("TURMA-A-EDITADA")
                .periodoLetivo("2026.1")
                .professorId(professor.getId())
                .build();

        String responseJSONString = driver.perform(put(URI_TURMA + "/" + turma.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertEquals(1, resultado.getEstudantes().size());

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, turmaRepository.findById(turma.getId()).orElseThrow().getEstudantes().size());
    }

    @Test
    @DisplayName("Teste para encerrar uma turma preservando estudantes e bloqueando novas associações")
    void testQuandoEncerramosUmaTurmaComEstudantes() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante maria = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        Estudante joao = salvarEstudante("João", "124210002", "joao@ccc.ufcg.edu.br");
        associarEstudante(turma, maria);

        String responseJSONString = driver.perform(put(URI_TURMA + "/" + turma.getId() + "/encerrar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertEquals(StatusTurma.ENCERRADA, resultado.getStatus());
        assertEquals(1, resultado.getEstudantes().size());

        driver.perform(post(URI_TURMA + "/" + turma.getId() + "/estudantes/" + joao.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(print());

        String estudantesJSONString = driver.perform(get(URI_TURMA + "/" + turma.getId() + "/estudantes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<EstudanteResponseDto> resultadoEstudantes = objectMapper.readValue(estudantesJSONString,
                new TypeReference<List<EstudanteResponseDto>>() {
                });
        assertEquals(1, resultadoEstudantes.size());
        assertEquals("124210001", resultadoEstudantes.get(0).getMatricula());
    }

    @Test
    @DisplayName("Teste para validar que um estudante pertence à turma")
    void testQuandoValidamosUmEstudanteAssociadoATurma() {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        turmaCrudService.adicionarEstudante(turma.getId(), estudante.getId());

        assertDoesNotThrow(() -> turmaCrudService.validarEstudantePertenceTurma(turma.getId(), estudante.getId()));
    }

    @Test
    @DisplayName("Teste para validar que um estudante não pertence à turma")
    void testQuandoValidamosUmEstudanteNaoAssociadoATurma() {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class,
                () -> turmaCrudService.validarEstudantePertenceTurma(turma.getId(), estudante.getId()));

        assertEquals(HttpStatus.CONFLICT.value(), excecao.getStatusCode().value());
    }
}
