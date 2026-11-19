package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.SubmissaoResponseDto;
import br.edu.ufcg.psoftarena.DTO.SubmissaoPostDto;
import br.edu.ufcg.psoftarena.Exception.EstudanteNaoPertenceTurmaException;
import br.edu.ufcg.psoftarena.Exception.RegraNegocioException;
import br.edu.ufcg.psoftarena.Model.State.Submissao.EmFila;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.FilaSubmissoes;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.HistoricoEstadoSubmissaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(OutputCaptureExtension.class)
class SubmissaoEncaminhamentoTests {

    private final SubmissaoRepository repository = mock(SubmissaoRepository.class);
    private final HistoricoEstadoSubmissaoRepository historicoRepository =
            mock(HistoricoEstadoSubmissaoRepository.class);
    private final SubmissaoCrudPadraoService service = new SubmissaoCrudPadraoService();

    private void prepararEnvio() {
        service.atividadeRepository = mock(AtividadeRepository.class);
        service.estudanteRepository = mock(EstudanteRepository.class);
        service.turmaCrudService = mock(TurmaCrudService.class);
        when(service.atividadeRepository.findById(3L)).thenReturn(Optional.of(Atividade.builder().id(3L)
                .prazo(LocalDateTime.now().plusDays(1)).turma(Turma.builder().id(4L).build()).build()));
        when(service.estudanteRepository.findById(2L))
                .thenReturn(Optional.of(Estudante.builder().id(2L).build()));
    }

    @Test
    void naoEnfileiraQuandoEstadoNaoAvanca() {
        prepararEnvio();
        service.estadoSubmissaoService = mock(EstadoSubmissaoService.class);
        service.enfileiradorSubmissoes = mock(EnfileiradorSubmissoes.class);
        when(repository.saveAndFlush(any(Submissao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(RegraNegocioException.class,
                () -> service.submeterSolucao(3L, SubmissaoPostDto.builder()
                        .estudanteId(2L).codigoFonte("class Main {}").build()));

        verify(service.estadoSubmissaoService).enfileirar(any(Submissao.class));
        verifyNoInteractions(service.enfileiradorSubmissoes);
    }

    @Test
    void rejeitaEstadoInesperadoRetornadoPelaPersistencia() {
        prepararEnvio();
        service.enfileiradorSubmissoes = mock(EnfileiradorSubmissoes.class);
        when(repository.saveAndFlush(any(Submissao.class))).thenAnswer(invocation -> {
            Submissao submissao = invocation.getArgument(0);
            submissao.mudarEstado(new EmFila());
            return submissao;
        });

        assertThrows(RegraNegocioException.class,
                () -> service.submeterSolucao(3L, SubmissaoPostDto.builder()
                        .estudanteId(2L).codigoFonte("class Main {}").build()));

        verify(historicoRepository).save(any());
        verifyNoInteractions(service.enfileiradorSubmissoes);
    }

    @Test
    void estudanteForaDaTurmaNaoSubmete() {
        prepararEnvio();
        service.enfileiradorSubmissoes = mock(EnfileiradorSubmissoes.class);
        doThrow(new EstudanteNaoPertenceTurmaException()).when(service.turmaCrudService)
                .validarEstudantePertenceTurma(4L, 2L);

        assertThrows(EstudanteNaoPertenceTurmaException.class,
                () -> service.submeterSolucao(3L, SubmissaoPostDto.builder()
                        .estudanteId(2L).codigoFonte("class Main {}").build()));

        verifyNoInteractions(repository, historicoRepository, service.enfileiradorSubmissoes);
    }

    @BeforeEach
    void preparar() {
        EstadoSubmissaoPadraoService estadoService = new EstadoSubmissaoPadraoService();
        estadoService.submissaoRepository = repository;
        estadoService.historicoEstadoSubmissaoRepository = historicoRepository;
        estadoService.notificadorEstudante = new NotificadorTerminal();
        service.submissaoRepository = repository;
        service.estadoSubmissaoService = estadoService;
        service.enfileiradorSubmissoes = new EnfileiradorSubmissoes();
    }

    @Test
    void deveSalvarComoSubmetidaEEncaminharAutomaticamente(CapturedOutput output) {
        service.atividadeRepository = mock(AtividadeRepository.class);
        service.estudanteRepository = mock(EstudanteRepository.class);
        service.turmaCrudService = mock(TurmaCrudService.class);
        Atividade atividade = Atividade.builder().id(3L)
                .prazo(LocalDateTime.now().plusDays(1))
                .turma(Turma.builder().id(4L).build()).build();
        when(service.atividadeRepository.findById(3L)).thenReturn(Optional.of(atividade));
        when(service.estudanteRepository.findById(2L))
                .thenReturn(Optional.of(Estudante.builder().id(2L).build()));
        List<String> estadosSalvos = new ArrayList<>();
        when(repository.saveAndFlush(any(Submissao.class))).thenAnswer(invocation -> {
            Submissao submissao = invocation.getArgument(0);
            estadosSalvos.add(submissao.getEstadoSubmissao().getCodigo());
            submissao.setId(1L);
            return submissao;
        });

        FilaSubmissoes fila = mock(FilaSubmissoes.class);
        service.enfileiradorSubmissoes = new EnfileiradorSubmissoes(fila);
        SubmissaoResponseDto resposta = service.submeterSolucao(3L, SubmissaoPostDto.builder()
                .estudanteId(2L).codigoFonte("class Main {}").build());
        assertEquals("EMFILA", resposta.getStatus());
        assertEquals(List.of("SUBMETIDA", "EMFILA"), estadosSalvos);
        verify(fila).adicionar(1L);

        String mensagem = "Notificação ao estudante 2: submissão 1 - registrada no estado SUBMETIDA";
        assertEquals(1L, output.getOut().lines().filter(mensagem::equals).count());
        String transicao = "Notificação ao estudante 2: submissão 1 - SUBMETIDA -> EMFILA";
        assertEquals(1L, output.getOut().lines().filter(transicao::equals).count());
    }

}
