package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.DTO.EstudantePostPutDto;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Classe de testes de concorrência de estudante")
public class EstudanteConcorrenciaTests {
    final String URI_ESTUDANTE = "/v1/estudantes";

    @Autowired
    MockMvc driver;

    @MockitoBean
    EstudanteRepository estudanteRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    EstudantePostPutDto dto() {
        return EstudantePostPutDto.builder()
                .nome("Maria")
                .matricula("124210001")
                .email("maria@ccc.ufcg.edu.br")
                .build();
    }

    void simularViolacaoDeUnicidadeAoGravar() {
        DataIntegrityViolationException violacao =
                new DataIntegrityViolationException("unique constraint: matricula");
        when(estudanteRepository.save(any(Estudante.class))).thenThrow(violacao);
        when(estudanteRepository.saveAndFlush(any(Estudante.class))).thenThrow(violacao);
    }

    @Test
    @DisplayName("Criação concorrente com mesma matrícula retorna 409, não 500")
    void testCriacaoConcorrenteComMatriculaDuplicada() throws Exception {
        when(estudanteRepository.existsByMatricula(anyString())).thenReturn(false);
        simularViolacaoDeUnicidadeAoGravar();

        driver.perform(post(URI_ESTUDANTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Edição concorrente para matrícula já usada retorna 409, não 500")
    void testEdicaoConcorrenteComMatriculaDuplicada() throws Exception {
        Estudante existente = Estudante.builder()
                .id(1L).nome("João").matricula("124210002").email("joao@ccc.ufcg.edu.br")
                .build();
        when(estudanteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(estudanteRepository.existsByMatriculaAndIdNot(anyString(), anyLong())).thenReturn(false);
        simularViolacaoDeUnicidadeAoGravar();

        driver.perform(put(URI_ESTUDANTE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Remoção de estudante associado a uma turma retorna 409, não 500")
    void testRemocaoDeEstudanteAssociadoATurma() throws Exception {
        Estudante existente = Estudante.builder()
                .id(1L).nome("João").matricula("124210002").email("joao@ccc.ufcg.edu.br")
                .build();
        when(estudanteRepository.findById(1L)).thenReturn(Optional.of(existente));
        doThrow(new DataIntegrityViolationException("foreign key: tb_turma_estudante"))
                .when(estudanteRepository).flush();

        driver.perform(delete(URI_ESTUDANTE + "/1"))
                .andExpect(status().isConflict());
    }
}
