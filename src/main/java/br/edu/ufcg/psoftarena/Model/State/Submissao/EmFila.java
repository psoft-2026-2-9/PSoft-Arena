package br.edu.ufcg.psoftarena.Model.State.Submissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class EmFila implements EstadoSubmissao{

    @Override
    public void proximoEstado(Submissao submissao) {
        submissao.setEstadoSubmissao(new Processando());
    }

    @Override
    public String getCodigo() { return "EMFILA";}
}
