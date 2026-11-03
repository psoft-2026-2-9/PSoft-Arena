package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
class ProblemaControllerTests {

    private static final String URI = "/problemas";

    @Autowired
    private MockMvc driver;

    @Autowired
    private ProblemaRepository repository;

    @Test
    void deveCriarProblema() throws Exception {
        long totalAnterior = repository.count();

        driver.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados("Arena")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Arena"))
                .andExpect(jsonPath("$.descricao").value("Descrição atualizada"))
                .andExpect(jsonPath("$.requisitos_funcionais[0]").value("Listar jogadores"))
                .andExpect(jsonPath("$.requisitos_de_projeto[0]").value("Usar Spring"))
                .andExpect(jsonPath("$.status").value("ATIVO"));

        assertEquals(totalAnterior + 1, repository.count());
    }

    @Test
    void deveRejeitarDadosInvalidos() throws Exception {
        long totalAnterior = repository.count();

        driver.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "titulo": "Arena",
                            "descricao": "Descrição válida",
                            "requisitos_funcionais": [],
                            "requisitos_de_projeto": ["Usar Java"]
                        }
                        """))
                .andExpect(status().isBadRequest());

        assertEquals(totalAnterior, repository.count());
    }

    @Test
    void deveListarProblemas() throws Exception {
        long id = salvarProblema().getId();

        driver.perform(get(URI))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].titulo").value("Arena"));
    }

    @Test
    void deveBuscarProblemaPorId() throws Exception {
        long id = salvarProblema().getId();

        driver.perform(get(URI + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Arena"));
    }

    @Test
    void deveRetornarNaoEncontradoParaIdInexistente() throws Exception {
        driver.perform(get(URI + "/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarProblema() throws Exception {
        long id = salvarProblema().getId();

        driver.perform(put(URI + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados("Arena revisada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Arena revisada"));

        Problema atualizado = repository.findById(id).orElseThrow();
        assertEquals("Arena revisada", atualizado.getTitulo());
        assertEquals("Descrição atualizada", atualizado.getDescricao());
        assertEquals(List.of("Listar jogadores"), atualizado.getRequisitosFuncionais());
        assertEquals(List.of("Usar Spring"), atualizado.getRequisitosDeProjeto());
        assertEquals(StatusProblema.ATIVO, atualizado.getStatus());
    }

    @Test
    void deveInativarProblema() throws Exception {
        long id = salvarProblema().getId();

        driver.perform(put(URI + "/{id}/inativar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("INATIVO"));

        assertEquals(StatusProblema.INATIVO, repository.findById(id).orElseThrow().getStatus());
    }

    @Test
    void deveExcluirProblema() throws Exception {
        long id = salvarProblema().getId();

        driver.perform(delete(URI + "/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(repository.existsById(id));
    }

    private Problema salvarProblema() {
        return repository.save(Problema.builder()
                .titulo("Arena")
                .descricao("Descrição inicial")
                .requisitosFuncionais(List.of("Cadastrar jogador"))
                .requisitosDeProjeto(List.of("Usar Java"))
                .status(StatusProblema.ATIVO)
                .build());
    }

    private String dados(String titulo) {
        return """
                {
                    "titulo": "%s",
                    "descricao": "Descrição atualizada",
                    "requisitos_funcionais": ["Listar jogadores"],
                    "requisitos_de_projeto": ["Usar Spring"]
                }
                """.formatted(titulo);
    }
}
