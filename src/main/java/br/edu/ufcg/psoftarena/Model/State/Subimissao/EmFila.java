package br.edu.ufcg.psoftarena.Model.State.Subimissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class EmFila implements EstadoSubimissao{

    @Override
    public void proximoEstado(Submissao submissao) {
        submissao.setEstadoSubimissao(new Processando());
    }

    @Override
    public String getCodigo() { return "EMFILA";}
}
