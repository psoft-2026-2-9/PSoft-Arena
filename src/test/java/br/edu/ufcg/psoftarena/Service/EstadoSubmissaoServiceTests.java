package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.HistoricoEstadoSubmissao;
import br.edu.ufcg.psoftarena.Model.State.Submissao.Concluida;
import br.edu.ufcg.psoftarena.Model.State.Submissao.EmFila;
import br.edu.ufcg.psoftarena.Model.State.Submissao.EstadoSubmissao;
import br.edu.ufcg.psoftarena.Model.State.Submissao.Processando;
import br.edu.ufcg.psoftarena.Model.State.Submissao.Submetida;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Repository.HistoricoEstadoSubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EstadoSubmissaoServiceTests {

    private final EstadoSubmissaoPadraoService service = new EstadoSubmissaoPadraoService();
    private final Estudante estudante = Estudante.builder().id(2L).build();

    @BeforeEach
    void setUp() {
        service.submissaoRepository = mock(SubmissaoRepository.class);
        service.historicoEstadoSubmissaoRepository = mock(HistoricoEstadoSubmissaoRepository.class);
        service.notificadorEstudante = mock(NotificadorEstudante.class);
    }

    Submissao submissao(long id, EstadoSubmissao estado) {
        return Submissao.builder().id(id).estudante(estudante).estadoSubmissao(estado).build();
    }

    @Test
    void operacoesSemMudancaNaoPersistemNemNotificam() {
        var concluida = new Concluida();
        Submissao finalizada = submissao(1L, concluida);
        var submetida = new Submetida();
        Submissao nova = submissao(2L, submetida);

        assertFalse(service.enfileirar(finalizada));
        assertFalse(service.iniciarProcessamento(finalizada));
        assertFalse(service.concluir(finalizada));
        assertFalse(service.falhar(finalizada));
        assertFalse(service.iniciarProcessamento(nova));
        assertFalse(service.falhar(nova));

        assertSame(concluida, finalizada.getEstadoSubmissao());
        assertSame(submetida, nova.getEstadoSubmissao());
        verifyNoInteractions(service.submissaoRepository, service.historicoEstadoSubmissaoRepository,
                service.notificadorEstudante);
    }

    @Test
    void transicaoValidaPersisteRegistraHistoricoENotifica() {
        Submissao submissao = submissao(7L, new EmFila());

        assertTrue(service.iniciarProcessamento(submissao));

        verify(service.submissaoRepository).saveAndFlush(submissao);
        ArgumentCaptor<HistoricoEstadoSubmissao> historico = ArgumentCaptor.forClass(HistoricoEstadoSubmissao.class);
        verify(service.historicoEstadoSubmissaoRepository).save(historico.capture());
        assertEquals("EMFILA", historico.getValue().getEstadoAnterior());
        assertEquals("PROCESSANDO", historico.getValue().getEstadoNovo());
        verify(service.notificadorEstudante).notificar(estudante, "submissão 7 - EMFILA -> PROCESSANDO");
    }

    @Test
    void conclusaoEFalhaPartemDeProcessando() {
        Submissao concluida = submissao(8L, new Processando());
        Submissao falha = submissao(9L, new Processando());

        assertTrue(service.concluir(concluida));
        assertTrue(service.falhar(falha));

        assertEquals("CONCLUIDA", concluida.getEstadoSubmissao().getCodigo());
        assertEquals("FALHA", falha.getEstadoSubmissao().getCodigo());
        verify(service.notificadorEstudante).notificar(estudante, "submissão 8 - PROCESSANDO -> CONCLUIDA");
        verify(service.notificadorEstudante).notificar(estudante, "submissão 9 - PROCESSANDO -> FALHA");
    }

    @Test
    void registroDaCriacaoNotificaEstadoInicial() {
        Submissao submissao = submissao(3L, new Submetida());

        service.registrarCriacao(submissao);

        verify(service.historicoEstadoSubmissaoRepository).save(any(HistoricoEstadoSubmissao.class));
        verify(service.notificadorEstudante).notificar(estudante, "submissão 3 - registrada no estado SUBMETIDA");
        verifyNoInteractions(service.submissaoRepository);
    }
}
