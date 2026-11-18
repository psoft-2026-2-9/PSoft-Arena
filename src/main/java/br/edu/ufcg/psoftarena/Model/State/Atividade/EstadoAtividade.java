package br.edu.ufcg.psoftarena.Model.State.Atividade;

import br.edu.ufcg.psoftarena.Model.Atividade;

public interface EstadoAtividade {

    boolean aceitaSubmissao();

    boolean concluir(Atividade atividade);

    String getCodigo();
}
