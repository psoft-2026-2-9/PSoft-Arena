package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.DTO.EstrategiaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstrategiaResponseDto;
import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import br.edu.ufcg.psoftarena.Repository.EstrategiaRepository;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Classe de testes de estratégia")
public class EstrategiaControllerTests {

    final String URI_ESTRATEGIA = "/estrategias";

    @Autowired
    MockMvc driver;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    EstrategiaRepository estrategiaRepository;

    @AfterEach
    void tearDown() {
        estrategiaRepository.deleteAll();
    }

    Estrategia salvarEstrategia(String nome, TipoEstrategia tipo, String descricao, String prompt) {
        return estrategiaRepository.save(Estrategia.builder()
                .nome(nome)
                .tipo(tipo)
                .descricao(descricao)
                .prompt(prompt)
                .build());
    }

    EstrategiaPostPutDto estrategiaPostPutDto(String nome, TipoEstrategia tipo, String descricao, String prompt) {
        return EstrategiaPostPutDto.builder()
                .nome(nome)
                .tipo(tipo)
                .descricao(descricao)
                .prompt(prompt)
                .build();
    }

    @Test
    @DisplayName("Teste para criação de uma estratégia")
    void testQuandoCriamosUmaEstrategia() throws Exception {
        EstrategiaPostPutDto dto = estrategiaPostPutDto(
                "Code Smells Básico",
                TipoEstrategia.CODE_SMELLS,
                "Avalia a presença de code smells",
                "Analise o código e identifique code smells.");

        String responseJSONString = driver.perform(post(URI_ESTRATEGIA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EstrategiaResponseDto resultado = objectMapper.readValue(responseJSONString, EstrategiaResponseDto.class);
        assertNotNull(resultado.getId());
        assertEquals("Code Smells Básico", resultado.getNome());
        assertEquals(TipoEstrategia.CODE_SMELLS, resultado.getTipo());
        assertEquals("Avalia a presença de code smells", resultado.getDescricao());
        assertEquals("Analise o código e identifique code smells.", resultado.getPrompt());
        assertEquals(1, estrategiaRepository.count());
    }

    @Test
    @DisplayName("Teste para criação de uma estratégia com campos em branco")
    void testQuandoCriamosUmaEstrategiaComCamposEmBranco() throws Exception {
        EstrategiaPostPutDto dto = estrategiaPostPutDto("", TipoEstrategia.CODE_SMELLS, "", "");

        driver.perform(post(URI_ESTRATEGIA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        assertEquals(0, estrategiaRepository.count());
    }

    @Test
    @DisplayName("Teste para criação de uma estratégia sem tipo")
    void testQuandoCriamosUmaEstrategiaSemTipo() throws Exception {
        EstrategiaPostPutDto dto = estrategiaPostPutDto(
                "Code Smells Básico",
                null,
                "Avalia a presença de code smells",
                "Analise o código e identifique code smells.");

        driver.perform(post(URI_ESTRATEGIA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        assertEquals(0, estrategiaRepository.count());
    }

    @Test
    @DisplayName("Teste para criação de uma estratégia com tipo inválido")
    void testQuandoCriamosUmaEstrategiaComTipoInvalido() throws Exception {
        Map<String, Object> dto = Map.of(
                "nome", "Code Smells Básico",
                "tipo", "TIPO_INVALIDO",
                "descricao", "Avalia a presença de code smells",
                "prompt", "Analise o código e identifique code smells.");

        driver.perform(post(URI_ESTRATEGIA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        assertEquals(0, estrategiaRepository.count());
    }

    @Test
    @DisplayName("Teste para buscar todas as estratégias")
    void testQuandoBuscamosTodasAsEstrategias() throws Exception {
        salvarEstrategia("Code Smells Básico", TipoEstrategia.CODE_SMELLS, "Descrição A", "Prompt A");
        salvarEstrategia("Padrões Básico", TipoEstrategia.APLICACAO_PADROES_PROJETO, "Descrição B", "Prompt B");

        String responseJSONString = driver.perform(get(URI_ESTRATEGIA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<EstrategiaResponseDto> resultado = objectMapper.readValue(
                responseJSONString, new TypeReference<List<EstrategiaResponseDto>>() {});
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Teste para buscar estratégias por tipo")
    void testQuandoBuscamosEstrategiasPorTipo() throws Exception {
        salvarEstrategia("Code Smells Básico", TipoEstrategia.CODE_SMELLS, "Descrição A", "Prompt A");
        salvarEstrategia("Padrões Básico", TipoEstrategia.APLICACAO_PADROES_PROJETO, "Descrição B", "Prompt B");

        String responseJSONString = driver.perform(get(URI_ESTRATEGIA)
                        .param("tipo", TipoEstrategia.CODE_SMELLS.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<EstrategiaResponseDto> resultado = objectMapper.readValue(
                responseJSONString, new TypeReference<List<EstrategiaResponseDto>>() {});
        assertEquals(1, resultado.size());
        assertEquals(TipoEstrategia.CODE_SMELLS, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("Teste para buscar uma estratégia pelo id")
    void testQuandoBuscamosUmaEstrategiaPeloId() throws Exception {
        Estrategia estrategia = salvarEstrategia(
                "Code Smells Básico", TipoEstrategia.CODE_SMELLS, "Descrição A", "Prompt A");

        String responseJSONString = driver.perform(get(URI_ESTRATEGIA + "/" + estrategia.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EstrategiaResponseDto resultado = objectMapper.readValue(responseJSONString, EstrategiaResponseDto.class);
        assertEquals(estrategia.getId(), resultado.getId());
        assertEquals("Code Smells Básico", resultado.getNome());
    }

    @Test
    @DisplayName("Teste para buscar uma estratégia inexistente")
    void testQuandoBuscamosUmaEstrategiaInexistente() throws Exception {
        driver.perform(get(URI_ESTRATEGIA + "/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para editar uma estratégia")
    void testQuandoEditamosUmaEstrategia() throws Exception {
        Estrategia estrategia = salvarEstrategia(
                "Code Smells Básico", TipoEstrategia.CODE_SMELLS, "Descrição A", "Prompt A");
        EstrategiaPostPutDto dto = estrategiaPostPutDto(
                "Padrões Avançado",
                TipoEstrategia.APLICACAO_PADROES_PROJETO,
                "Descrição atualizada",
                "Prompt atualizado");

        String responseJSONString = driver.perform(put(URI_ESTRATEGIA + "/" + estrategia.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EstrategiaResponseDto resultado = objectMapper.readValue(responseJSONString, EstrategiaResponseDto.class);
        assertEquals(estrategia.getId(), resultado.getId());
        assertEquals("Padrões Avançado", resultado.getNome());
        assertEquals(TipoEstrategia.APLICACAO_PADROES_PROJETO, resultado.getTipo());
        assertEquals("Descrição atualizada", resultado.getDescricao());
        assertEquals("Prompt atualizado", resultado.getPrompt());
    }

    @Test
    @DisplayName("Teste para editar uma estratégia inexistente")
    void testQuandoEditamosUmaEstrategiaInexistente() throws Exception {
        EstrategiaPostPutDto dto = estrategiaPostPutDto(
                "Padrões Avançado",
                TipoEstrategia.APLICACAO_PADROES_PROJETO,
                "Descrição atualizada",
                "Prompt atualizado");

        driver.perform(put(URI_ESTRATEGIA + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Teste para criar duas estratégias do mesmo tipo com prompts diferentes")
    void testQuandoCriamosDuasEstrategiasDoMesmoTipoComPromptsDiferentes() throws Exception {
        EstrategiaPostPutDto primeira = estrategiaPostPutDto(
                "Code Smells Básico",
                TipoEstrategia.CODE_SMELLS,
                "Descrição A",
                "Prompt A");
        EstrategiaPostPutDto segunda = estrategiaPostPutDto(
                "Code Smells Avançado",
                TipoEstrategia.CODE_SMELLS,
                "Descrição B",
                "Prompt B");

        driver.perform(post(URI_ESTRATEGIA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primeira)))
                .andExpect(status().isCreated())
                .andDo(print());

        driver.perform(post(URI_ESTRATEGIA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(segunda)))
                .andExpect(status().isCreated())
                .andDo(print());

        List<Estrategia> estrategias = estrategiaRepository.findAll();
        assertEquals(2, estrategias.size());
        assertEquals(2, estrategias.stream()
                .filter(estrategia -> estrategia.getTipo() == TipoEstrategia.CODE_SMELLS)
                .count());
        assertTrue(estrategias.stream()
                .map(Estrategia::getPrompt)
                .toList()
                .containsAll(List.of("Prompt A", "Prompt B")));
    }

    @Test
    @DisplayName("Teste para garantir que editar o prompt de uma estratégia não altera a outra")
    void testQuandoEditamosOPromptDeUmaEstrategiaNaoAlteramosAOutra() throws Exception {
        Estrategia primeira = salvarEstrategia(
                "Code Smells Básico", TipoEstrategia.CODE_SMELLS, "Descrição A", "Prompt A");
        Estrategia segunda = salvarEstrategia(
                "Code Smells Avançado", TipoEstrategia.CODE_SMELLS, "Descrição B", "Prompt B");

        EstrategiaPostPutDto dto = estrategiaPostPutDto(
                "Code Smells Básico",
                TipoEstrategia.CODE_SMELLS,
                "Descrição A",
                "Prompt atualizado");

        driver.perform(put(URI_ESTRATEGIA + "/" + primeira.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());

        assertEquals("Prompt atualizado", estrategiaRepository.findById(primeira.getId())
                .orElseThrow()
                .getPrompt());

        String responseJSONString = driver.perform(get(URI_ESTRATEGIA + "/" + segunda.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EstrategiaResponseDto resultado = objectMapper.readValue(responseJSONString, EstrategiaResponseDto.class);
        assertEquals("Prompt B", resultado.getPrompt());
    }
}
