package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.FilaSubmissoes;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class EnfileiradorSubmissoes {

    public void enfileirarAposCommit(Long submissaoId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            FilaSubmissoes.getInstance().adicionar(submissaoId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                FilaSubmissoes.getInstance().adicionar(submissaoId);
            }
        });
    }
}
