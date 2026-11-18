package br.edu.ufcg.psoftarena.Model.State.Subimissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class Processando implements EstadoSubimissao{

    @Override
    public void proximoEstado(Submissao submissao) {
        submissao.setEstadoSubmissao(new Concluida());
    }

    @Override
    public void falhar(Submissao submissao) {
        submissao.setEstadoSubmissao(new Falha());
    }

    @Override
    public String getCodigo() { return "PROCESSANDO";}
}
