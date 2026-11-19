package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.FilaSubmissoes;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class EnfileiradorSubmissoes {

    private final FilaSubmissoes fila;

    public EnfileiradorSubmissoes() {
        this(FilaSubmissoes.getInstance());
    }

    EnfileiradorSubmissoes(FilaSubmissoes fila) {
        this.fila = fila;
    }

    public void enfileirarAposCommit(Long submissaoId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            fila.adicionar(submissaoId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fila.adicionar(submissaoId);
            }
        });
    }
}
