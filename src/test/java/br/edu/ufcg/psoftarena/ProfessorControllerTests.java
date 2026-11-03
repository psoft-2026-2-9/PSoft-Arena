package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfessorControllerTests {

    private static final String URI = "/professores";

    @Autowired
    private MockMvc driver;

    @Autowired
    private ProfessorRepository repository;

    @Test
    void deveCriarProfessor() throws Exception {
        long totalAnterior = repository.count();

        driver.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados("Maria")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Maria"));

        assertEquals(totalAnterior + 1, repository.count());
    }

    @Test
    void deveRejeitarDadosInvalidos() throws Exception {
        long totalAnterior = repository.count();

        driver.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados(" ")))
                .andExpect(status().isBadRequest());

        assertEquals(totalAnterior, repository.count());
    }

    @Test
    void deveListarProfessors() throws Exception {
        long id = salvarProfessor().getId();

        driver.perform(get(URI))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].nome").value("Maria"));
    }

    @Test
    void deveBuscarProfessorPorId() throws Exception {
        long id = salvarProfessor().getId();

        driver.perform(get(URI + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Maria"));
    }

    @Test
    void deveRetornarNaoEncontradoParaIdInexistente() throws Exception {
        driver.perform(get(URI + "/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarProfessor() throws Exception {
        long id = salvarProfessor().getId();

        driver.perform(put(URI + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados("Maria Silva")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));

        Professor atualizado = repository.findById(id).orElseThrow();
        assertEquals("Maria Silva", atualizado.getNome());
    }

    @Test
    void deveExcluirProfessor() throws Exception {
        long id = salvarProfessor().getId();

        driver.perform(delete(URI + "/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(repository.existsById(id));
    }

    private Professor salvarProfessor() {
        return repository.save(Professor.builder()
                .nome("Maria")
                .build());
    }

    private String dados(String nome) {
        return "{\"nome\":\"" + nome + "\"}";
    }
}
