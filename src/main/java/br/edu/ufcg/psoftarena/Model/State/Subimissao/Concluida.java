package br.edu.ufcg.psoftarena.Model.State.Subimissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class Concluida implements EstadoSubimissao{

    @Override
    public void proximoEstado(Submissao submissao) {
        return;
    }

    @Override
    public String getCodigo() { return "CONCLUIDO";}
}
