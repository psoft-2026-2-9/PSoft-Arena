package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.DTO.EstudantePostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstudanteResponseDto;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Classe de testes de estudante")
public class EstudanteControllerTests {
    final String URI_ESTUDANTE = "/v1/estudantes";

    @Autowired
    MockMvc driver;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    EstudanteRepository estudanteRepository;

    @AfterEach
    void tearDown() {
        estudanteRepository.deleteAll();
    }

    Estudante salvarEstudante(String nome, String matricula, String email) {
        return estudanteRepository.save(
                Estudante.builder()
                        .nome(nome)
                        .matricula(matricula)
                        .email(email)
                        .build()
        );
    }

    @Test
    @DisplayName("Teste para criação de um estudante")
    void testQuandoCriamosUmEstudante() throws Exception {
        EstudantePostPutDto estudantePostPutDto = EstudantePostPutDto.builder()
                .nome("Maria")
                .matricula("124210001")
                .email("maria@ccc.ufcg.edu.br")
                .build();

        String responseJSONString = driver.perform(post(URI_ESTUDANTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estudantePostPutDto)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EstudanteResponseDto resultado = objectMapper.readValue(responseJSONString, EstudanteResponseDto.class);
        assertEquals("Maria", resultado.getNome());
        assertEquals("124210001", resultado.getMatricula());
        assertEquals("maria@ccc.ufcg.edu.br", resultado.getEmail());
        assertEquals(1, estudanteRepository.findAll().size());
    }

    @Test
    @DisplayName("Teste para criação de um estudante com nome vazio")
    void testQuandoCriamosUmEstudanteComNomeVazio() throws Exception {
        EstudantePostPutDto estudantePostPutDto = EstudantePostPutDto.builder()
                .nome("")
                .matricula("124210001")
                .email("maria@ccc.ufcg.edu.br")
                .build();

        driver.perform(post(URI_ESTUDANTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estudantePostPutDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        assertEquals(0, estudanteRepository.findAll().size());
    }

    @Test
    @DisplayName("Teste para criação de um estudante com e-mail inválido")
    void testQuandoCriamosUmEstudanteComEmailInvalido() throws Exception {
        EstudantePostPutDto estudantePostPutDto = EstudantePostPutDto.builder()
                .nome("Maria")
                .matricula("124210001")
                .email("maria.ufcg")
                .build();

        driver.perform(post(URI_ESTUDANTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estudantePostPutDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        assertEquals(0, estudanteRepository.findAll().size());
    }

    @Test
    @DisplayName("Teste para buscar todos estudantes")
    void testQuandoBuscamosTodosEstudantes() throws Exception {
        salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        salvarEstudante("João", "124210002", "joao@ccc.ufcg.edu.br");

        String responseJSONString = driver.perform(get(URI_ESTUDANTE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<EstudanteResponseDto> resultado = objectMapper.readValue(responseJSONString, new TypeReference<List<EstudanteResponseDto>>(){});
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Teste para buscar um estudante pelo id")
    void testQuandoBuscamosUmEstudante() throws Exception {
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");

        String responseJSONString = driver.perform(get(URI_ESTUDANTE + "/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EstudanteResponseDto resultado = objectMapper.readValue(responseJSONString, EstudanteResponseDto.class);
        assertEquals(estudante.getId(), resultado.getId());
        assertEquals("124210001", resultado.getMatricula());
    }

    @Test
    @DisplayName("Teste para editar um estudante")
    void testQuandoEditamosUmEstudante() throws Exception {
        Estudante estudante = salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        EstudantePostPutDto estudantePostPutDto = EstudantePostPutDto.builder()
                .nome("Maria Silva")
                .matricula("124210001")
                .email("maria.silva@ccc.ufcg.edu.br")
                .build();

        String responseJSONString = driver.perform(put(URI_ESTUDANTE + "/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estudantePostPutDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EstudanteResponseDto resultado = objectMapper.readValue(responseJSONString, EstudanteResponseDto.class);
        assertEquals(estudante.getId(), resultado.getId());
        assertEquals("Maria Silva", resultado.getNome());
        assertEquals("maria.silva@ccc.ufcg.edu.br", resultado.getEmail());
    }

    @Test
    @DisplayName("Teste para criação de um estudante com matrícula duplicada")
    void testQuandoCriamosUmEstudanteComMatriculaDuplicada() throws Exception {
        salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        EstudantePostPutDto estudantePostPutDto = EstudantePostPutDto.builder()
                .nome("João")
                .matricula("124210001")
                .email("joao@ccc.ufcg.edu.br")
                .build();

        driver.perform(post(URI_ESTUDANTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estudantePostPutDto)))
                .andExpect(status().isConflict())
                .andDo(print());

        assertEquals(1, estudanteRepository.findAll().size());
    }

    @Test
    @DisplayName("Teste para buscar um estudante inexistente")
    void testQuandoBuscamosUmEstudanteInexistente() throws Exception {
        driver.perform(get(URI_ESTUDANTE + "/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para editar um estudante inexistente")
    void testQuandoEditamosUmEstudanteInexistente() throws Exception {
        EstudantePostPutDto estudantePostPutDto = EstudantePostPutDto.builder()
                .nome("Maria")
                .matricula("124210001")
                .email("maria@ccc.ufcg.edu.br")
                .build();

        driver.perform(put(URI_ESTUDANTE + "/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estudantePostPutDto)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para editar um estudante com matrícula de outro estudante")
    void testQuandoEditamosUmEstudanteComMatriculaDeOutro() throws Exception {
        salvarEstudante("Maria", "124210001", "maria@ccc.ufcg.edu.br");
        Estudante estudante = salvarEstudante("João", "124210002", "joao@ccc.ufcg.edu.br");
        EstudantePostPutDto estudantePostPutDto = EstudantePostPutDto.builder()
                .nome("João")
                .matricula("124210001")
                .email("joao@ccc.ufcg.edu.br")
                .build();

        driver.perform(put(URI_ESTUDANTE + "/" + estudante.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estudantePostPutDto)))
                .andExpect(status().isConflict())
                .andDo(print());

        assertEquals("124210002", estudanteRepository.findById(estudante.getId()).get().getMatricula());
    }
}