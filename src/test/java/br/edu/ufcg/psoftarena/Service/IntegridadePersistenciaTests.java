package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.EstudantePostPutDto;
import br.edu.ufcg.psoftarena.DTO.SubmissaoPostDto;
import br.edu.ufcg.psoftarena.Exception.RegraNegocioException;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IntegridadePersistenciaTests {

    @Test
    void falhaDeIntegridadeDoEstudanteNaoAfirmaMatriculaDuplicada() {
        EstudanteCrudPadraoService service = new EstudanteCrudPadraoService();
        service.estudanteRepository = mock(EstudanteRepository.class);
        service.modelMapper = new ModelMapper();
        DataIntegrityViolationException causa = new DataIntegrityViolationException("NOT NULL: nome");
        when(service.estudanteRepository.saveAndFlush(any(Estudante.class))).thenThrow(causa);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.criarEstudante(EstudantePostPutDto.builder()
                        .nome("Ana").matricula("123").email("ana@ufcg.edu.br").build()));

        assertSame(causa, erro.getCause());
        assertEquals("Não foi possível salvar o estudante devido a uma restrição de integridade", erro.getMessage());
    }

    @Test
    void falhaDeIntegridadeDaSubmissaoNaoAfirmaConcorrenciaNemEnfileira() {
        SubmissaoCrudPadraoService service = new SubmissaoCrudPadraoService();
        service.submissaoRepository = mock(SubmissaoRepository.class);
        service.atividadeRepository = mock(AtividadeRepository.class);
        service.estudanteRepository = mock(EstudanteRepository.class);
        service.turmaCrudService = mock(TurmaCrudService.class);
        service.estadoSubmissaoService = mock(EstadoSubmissaoService.class);
        service.enfileiradorSubmissoes = mock(EnfileiradorSubmissoes.class);
        Atividade atividade = Atividade.builder().id(1L).prazo(LocalDateTime.now().plusDays(1))
                .turma(Turma.builder().id(3L).build()).build();
        when(service.atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
        when(service.estudanteRepository.findById(2L))
                .thenReturn(Optional.of(Estudante.builder().id(2L).build()));
        DataIntegrityViolationException causa = new DataIntegrityViolationException("NOT NULL: codigo_fonte");
        when(service.submissaoRepository.saveAndFlush(any(Submissao.class))).thenThrow(causa);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.submeterSolucao(1L, SubmissaoPostDto.builder()
                        .estudanteId(2L).codigoFonte("class Main {}").build()));

        assertSame(causa, erro.getCause());
        assertEquals("Não foi possível salvar a submissão devido a uma restrição de integridade", erro.getMessage());
        verifyNoInteractions(service.estadoSubmissaoService, service.enfileiradorSubmissoes);
    }

    @Test
    void falhaDeIntegridadeNaRemocaoPreservaCausaSemAfirmarVinculoComTurma() {
        EstudanteCrudPadraoService service = new EstudanteCrudPadraoService();
        service.estudanteRepository = mock(EstudanteRepository.class);
        service.turmaRepository = mock(br.edu.ufcg.psoftarena.Repository.TurmaRepository.class);
        service.submissaoRepository = mock(SubmissaoRepository.class);
        when(service.estudanteRepository.findById(1L))
                .thenReturn(Optional.of(Estudante.builder().id(1L).build()));
        DataIntegrityViolationException causa = new DataIntegrityViolationException("foreign key: outra_relacao");
        doThrow(causa).when(service.estudanteRepository).flush();

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> service.removerEstudante(1L));

        assertSame(causa, erro.getCause());
        assertEquals("Não foi possível remover o estudante devido a uma restrição de integridade", erro.getMessage());
    }
}
