package br.edu.ufcg.psoftarena.Model.State.Atividade;

import br.edu.ufcg.psoftarena.Model.Atividade;

public class AtividadeAberta implements EstadoAtividade {

    @Override
    public boolean aceitaSubmissao() {
        return true;
    }

    @Override
    public boolean concluir(Atividade atividade) {
        atividade.mudarEstado(new AtividadeConcluida());
        return true;
    }

    @Override
    public String getCodigo() {
        return "ABERTA";
    }
}
