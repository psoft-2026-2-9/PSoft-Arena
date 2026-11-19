package br.edu.ufcg.psoftarena.Model.State.Submissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class Submetida implements EstadoSubmissao {

    @Override
    public boolean enfileirar(Submissao submissao) {
        submissao.mudarEstado(new EmFila());
        return true;
    }

    @Override
    public String getCodigo() {
        return "SUBMETIDA";
    }
}
