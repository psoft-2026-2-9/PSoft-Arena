package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.SubmissaoResponseDto;
import br.edu.ufcg.psoftarena.Exception.RecursoNaoEncontradoException;
import br.edu.ufcg.psoftarena.Exception.RegraNegocioException;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.FilaSubmissoes;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.EmFila;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SubmissaoEncaminhamentoTests {

    private final SubmissaoRepository repository = mock(SubmissaoRepository.class);
    private final SubmissaoCrudPadraoService service = new SubmissaoCrudPadraoService();

    @BeforeEach
    void preparar() {
        service.submissaoRepository = repository;
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void limpar() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void deveAdicionarNaFilaSomenteAposCommit() {
        Submissao submissao = Submissao.builder()
                .id(1L)
                .estudante(Estudante.builder().id(2L).build())
                .atividade(Atividade.builder().id(3L).build())
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(submissao));
        FilaSubmissoes fila = mock(FilaSubmissoes.class);

        try (MockedStatic<FilaSubmissoes> singleton = mockStatic(FilaSubmissoes.class)) {
            singleton.when(FilaSubmissoes::getInstance).thenReturn(fila);

            SubmissaoResponseDto resposta = service.encaminharParaFila(1L);

            assertEquals("EMFILA", resposta.getStatus());
            verify(repository).saveAndFlush(submissao);
            verifyNoInteractions(fila);

            for (TransactionSynchronization synchronization :
                    TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            verify(fila).adicionar(1L);
        }
    }

    @Test
    void deveRejeitarSubmissaoInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> service.encaminharParaFila(1L));

        assertEquals("Submissão não encontrada", erro.getMessage());
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
    }

    @Test
    void deveRejeitarSubmissaoJaEncaminhada() {
        Submissao submissao = Submissao.builder().id(1L)
                .estadoSubimissao(new EmFila()).build();
        when(repository.findById(1L)).thenReturn(Optional.of(submissao));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.encaminharParaFila(1L));

        assertEquals("Somente submissões no estado Submetida podem ser encaminhadas para a fila",
                erro.getMessage());
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
    }
}
