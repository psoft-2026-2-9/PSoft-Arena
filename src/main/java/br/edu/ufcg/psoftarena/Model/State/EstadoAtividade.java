package br.edu.ufcg.psoftarena.Model.State;

import br.edu.ufcg.psoftarena.ENUM.StatusAtividade;
import br.edu.ufcg.psoftarena.Model.Atividade;

public interface EstadoAtividade {

    boolean aceitaSubmissao();

    boolean concluir(Atividade atividade);

    StatusAtividade getStatus();
}
