package br.edu.ufcg.psoftarena.Model.State.Submissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class Concluida implements EstadoSubmissao{

    @Override
    public void proximoEstado(Submissao submissao) {
        return;
    }

    @Override
    public String getCodigo() { return "CONCLUIDA";}
}
