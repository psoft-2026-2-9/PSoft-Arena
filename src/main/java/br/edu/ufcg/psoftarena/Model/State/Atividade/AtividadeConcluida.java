package br.edu.ufcg.psoftarena.Model.State.Atividade;

import br.edu.ufcg.psoftarena.Model.Atividade;

public class AtividadeConcluida implements EstadoAtividade {

    @Override
    public boolean aceitaSubmissao() {
        return false;
    }

    @Override
    public boolean concluir(Atividade atividade) {
        return false;
    }

    @Override
    public String getCodigo() {
        return "CONCLUIDA";
    }
}
