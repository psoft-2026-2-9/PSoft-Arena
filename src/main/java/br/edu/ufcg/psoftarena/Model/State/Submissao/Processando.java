package br.edu.ufcg.psoftarena.Model.State.Submissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class Processando implements EstadoSubmissao {

    @Override
    public boolean concluir(Submissao submissao) {
        submissao.mudarEstado(new Concluida());
        return true;
    }

    @Override
    public boolean falhar(Submissao submissao) {
        submissao.mudarEstado(new Falha());
        return true;
    }

    @Override
    public String getCodigo() {
        return "PROCESSANDO";
    }
}
