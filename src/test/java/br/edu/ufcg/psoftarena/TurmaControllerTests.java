package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.DTO.TurmaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.TurmaResponseDto;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Classe de testes de turma")
public class TurmaControllerTests {

    final String URI_TURMA = "/turmas";

    @Autowired
    MockMvc driver;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    TurmaRepository turmaRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    EntityManager entityManager;

    @AfterEach
    void tearDown() {
        turmaRepository.deleteAll();
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

    TurmaPostPutDto turmaPostPutDto(String nome, String codigo, String periodoLetivo, Long professorId) {
        return TurmaPostPutDto.builder()
                .nome(nome)
                .codigo(codigo)
                .periodoLetivo(periodoLetivo)
                .professorId(professorId)
                .build();
    }

    @Test
    @DisplayName("Teste para criação de uma turma")
    void testQuandoCriamosUmaTurma() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        TurmaPostPutDto dto = turmaPostPutDto("Turma A", "TURMA-A", "2026.2", professor.getId());

        String responseJSONString = driver.perform(post(URI_TURMA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertEquals("Turma A", resultado.getNome());
        assertEquals("TURMA-A", resultado.getCodigo());
        assertEquals("2026.2", resultado.getPeriodoLetivo());
        assertEquals(StatusTurma.ATIVA, resultado.getStatus());
        assertNotNull(resultado.getProfessor());
        assertEquals(professor.getId(), resultado.getProfessor().getId().longValue());
        assertEquals("Professor A", resultado.getProfessor().getNome());
        assertEquals(1, turmaRepository.findAll().size());
    }

    @Test
    @DisplayName("Teste para criação de uma turma com campos em branco")
    void testQuandoCriamosUmaTurmaComCamposEmBranco() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        TurmaPostPutDto dto = turmaPostPutDto("", "", "", professor.getId());

        driver.perform(post(URI_TURMA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        assertEquals(0, turmaRepository.findAll().size());
    }

    @Test
    @DisplayName("Teste para criação de uma turma com professor inexistente")
    void testQuandoCriamosUmaTurmaComProfessorInexistente() throws Exception {
        TurmaPostPutDto dto = turmaPostPutDto("Turma A", "TURMA-A", "2026.2", 999L);

        driver.perform(post(URI_TURMA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andDo(print());

        assertEquals(0, turmaRepository.findAll().size());
    }

    @Test
    @DisplayName("Teste para criação de uma turma com código duplicado")
    void testQuandoCriamosUmaTurmaComCodigoDuplicado() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        TurmaPostPutDto dto = turmaPostPutDto("Turma B", "TURMA-A", "2026.2", professor.getId());

        driver.perform(post(URI_TURMA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andDo(print());

        assertEquals(1, turmaRepository.findAll().size());
    }

    @Test
    @DisplayName("Teste para buscar todas as turmas")
    void testQuandoBuscamosTodasAsTurmas() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        salvarTurma("Turma B", "TURMA-B", "2026.1", professor, StatusTurma.ENCERRADA);

        String responseJSONString = driver.perform(get(URI_TURMA)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<TurmaResponseDto> resultado = objectMapper.readValue(responseJSONString,
                new TypeReference<List<TurmaResponseDto>>() {
                });
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Teste para buscar turmas por professor")
    void testQuandoBuscamosTurmasPorProfessor() throws Exception {
        Professor professorA = salvarProfessor("Professor A");
        Professor professorB = salvarProfessor("Professor B");
        salvarTurma("Turma A", "TURMA-A", "2026.2", professorA, StatusTurma.ATIVA);
        salvarTurma("Turma B", "TURMA-B", "2026.2", professorB, StatusTurma.ATIVA);

        String responseJSONString = driver.perform(get(URI_TURMA)
                .param("professorId", String.valueOf(professorA.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<TurmaResponseDto> resultado = objectMapper.readValue(responseJSONString,
                new TypeReference<List<TurmaResponseDto>>() {
                });
        assertEquals(1, resultado.size());
        assertEquals(professorA.getId(), resultado.get(0).getProfessor().getId().longValue());
    }

    @Test
    @DisplayName("Teste para buscar uma turma pelo id")
    void testQuandoBuscamosUmaTurmaPeloId() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);

        String responseJSONString = driver.perform(get(URI_TURMA + "/" + turma.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertEquals(turma.getId(), resultado.getId().longValue());
        assertEquals("TURMA-A", resultado.getCodigo());
    }

    @Test
    @DisplayName("Teste para buscar uma turma inexistente")
    void testQuandoBuscamosUmaTurmaInexistente() throws Exception {
        driver.perform(get(URI_TURMA + "/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para editar uma turma ativa")
    void testQuandoEditamosUmaTurmaAtiva() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        TurmaPostPutDto dto = turmaPostPutDto("Turma A Editada", "TURMA-A-2", "2026.1", professor.getId());

        String responseJSONString = driver.perform(put(URI_TURMA + "/" + turma.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertEquals(turma.getId(), resultado.getId().longValue());
        assertEquals("Turma A Editada", resultado.getNome());
        assertEquals("TURMA-A-2", resultado.getCodigo());
        assertEquals("2026.1", resultado.getPeriodoLetivo());
        assertEquals(StatusTurma.ATIVA, resultado.getStatus());
    }

    @Test
    @DisplayName("Teste para editar uma turma encerrada")
    void testQuandoEditamosUmaTurmaEncerrada() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ENCERRADA);
        TurmaPostPutDto dto = turmaPostPutDto("Turma A Editada", "TURMA-A", "2026.2", professor.getId());

        driver.perform(put(URI_TURMA + "/" + turma.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para editar uma turma com código já existente")
    void testQuandoEditamosUmaTurmaComCodigoJaExistente() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turmaA = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        salvarTurma("Turma B", "TURMA-B", "2026.2", professor, StatusTurma.ATIVA);
        TurmaPostPutDto dto = turmaPostPutDto("Turma A", "TURMA-B", "2026.2", professor.getId());

        driver.perform(put(URI_TURMA + "/" + turmaA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para encerrar uma turma")
    void testQuandoEncerramosUmaTurma() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);

        String responseJSONString = driver.perform(put(URI_TURMA + "/" + turma.getId() + "/encerrar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertEquals(StatusTurma.ENCERRADA, resultado.getStatus());

        driver.perform(get(URI_TURMA + "/" + turma.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para encerrar uma turma já encerrada")
    void testQuandoEncerramosUmaTurmaJaEncerrada() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ENCERRADA);

        driver.perform(put(URI_TURMA + "/" + turma.getId() + "/encerrar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para editar uma turma com professor inexistente não altera a turma")
    void testQuandoEditamosUmaTurmaComProfessorInexistente() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        TurmaPostPutDto dto = turmaPostPutDto("Turma A Editada", "TURMA-A-2", "2026.1", 999L);

        driver.perform(put(URI_TURMA + "/" + turma.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andDo(print());

        Turma turmaAposErro = turmaRepository.findById(turma.getId()).orElseThrow();
        assertEquals("Turma A", turmaAposErro.getNome());
        assertEquals("TURMA-A", turmaAposErro.getCodigo());
        assertEquals("2026.2", turmaAposErro.getPeriodoLetivo());
        assertEquals(professor.getId(), turmaAposErro.getProfessor().getId());
    }

    @Test
    @DisplayName("Teste para editar uma turma enviando professor_id nulo")
    void testQuandoEditamosUmaTurmaComProfessorIdNulo() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", professor, StatusTurma.ATIVA);
        TurmaPostPutDto dto = turmaPostPutDto("Turma A Editada", "TURMA-A-2", "2026.1", null);

        driver.perform(put(URI_TURMA + "/" + turma.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        Turma turmaAposErro = turmaRepository.findById(turma.getId()).orElseThrow();
        assertEquals("Turma A", turmaAposErro.getNome());
        assertNotNull(turmaAposErro.getProfessor());
    }

    @Test
    @DisplayName("Teste para excluir um professor desvincula as turmas dele")
    void testQuandoExcluimosUmProfessorComTurmas() throws Exception {
        Professor professorA = salvarProfessor("Professor A");
        Professor professorB = salvarProfessor("Professor B");
        Turma turmaAtiva = salvarTurma("Turma A", "TURMA-A", "2026.2", professorA, StatusTurma.ATIVA);
        Turma turmaEncerrada = salvarTurma("Turma B", "TURMA-B", "2026.1", professorA, StatusTurma.ENCERRADA);
        Turma turmaDeOutro = salvarTurma("Turma C", "TURMA-C", "2026.2", professorB, StatusTurma.ATIVA);
        entityManager.flush();
        entityManager.clear();

        driver.perform(delete("/professores/" + professorA.getId()))
                .andExpect(status().isNoContent())
                .andDo(print());

        // O desvínculo é feito pelo banco (ON DELETE SET NULL): força o delete e relê
        // do banco
        entityManager.flush();
        entityManager.clear();

        assertEquals(false, professorRepository.existsById(professorA.getId()));
        assertEquals(3, turmaRepository.findAll().size());
        assertNull(turmaRepository.findById(turmaAtiva.getId()).orElseThrow().getProfessor());
        assertNull(turmaRepository.findById(turmaEncerrada.getId()).orElseThrow().getProfessor());
        assertEquals(professorB.getId(),
                turmaRepository.findById(turmaDeOutro.getId()).orElseThrow().getProfessor().getId());

        String responseJSONString = driver.perform(get(URI_TURMA + "/" + turmaAtiva.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertEquals("TURMA-A", resultado.getCodigo());
        assertNull(resultado.getProfessor());
    }

    @Test
    @DisplayName("Teste para editar uma turma sem professor informando um novo professor")
    void testQuandoEditamosUmaTurmaSemProfessor() throws Exception {
        Professor professor = salvarProfessor("Professor A");
        Turma turma = salvarTurma("Turma A", "TURMA-A", "2026.2", null, StatusTurma.ATIVA);
        TurmaPostPutDto dto = turmaPostPutDto("Turma A", "TURMA-A", "2026.2", professor.getId());

        String responseJSONString = driver.perform(put(URI_TURMA + "/" + turma.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        TurmaResponseDto resultado = objectMapper.readValue(responseJSONString, TurmaResponseDto.class);
        assertNotNull(resultado.getProfessor());
        assertEquals(professor.getId(), resultado.getProfessor().getId().longValue());
    }
}
