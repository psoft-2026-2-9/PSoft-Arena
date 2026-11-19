package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.FilaSubmissoes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DisplayName("Classe de testes do enfileiramento de submissões")
class EnfileiradorSubmissoesTests {

    EnfileiradorSubmissoes enfileirador;

    @Autowired
    TransactionTemplate transactionTemplate;

    FilaSubmissoes fila;

    @BeforeEach
    void setUp() {
        fila = mock(FilaSubmissoes.class);
        enfileirador = new EnfileiradorSubmissoes(fila);
    }

    @Test
    @DisplayName("Submissão só entra na fila após o commit da transação")
    void deveEnfileirarAposCommit() {
        transactionTemplate.executeWithoutResult(status -> {
            enfileirador.enfileirarAposCommit(1L);
            verify(fila, never()).adicionar(1L);
        });

        verify(fila).adicionar(1L);
    }

    @Test
    @DisplayName("Submissão não entra na fila quando a transação é desfeita")
    void naoDeveEnfileirarAposRollback() {
        transactionTemplate.executeWithoutResult(status -> {
            enfileirador.enfileirarAposCommit(2L);
            status.setRollbackOnly();
        });

        verify(fila, never()).adicionar(2L);
    }

    @Test
    @DisplayName("Sem transação ativa a submissão entra na fila imediatamente")
    void deveEnfileirarImediatamenteSemTransacao() {
        enfileirador.enfileirarAposCommit(3L);

        verify(fila).adicionar(3L);
    }
}
