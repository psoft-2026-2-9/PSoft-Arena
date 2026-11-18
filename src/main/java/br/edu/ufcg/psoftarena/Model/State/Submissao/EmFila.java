package br.edu.ufcg.psoftarena.Model.State.Submissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class EmFila implements EstadoSubmissao {

    @Override
    public boolean processar(Submissao submissao) {
        submissao.mudarEstado(new Processando());
        return true;
    }

    @Override
    public String getCodigo() {
        return "EMFILA";
    }
}
