package br.edu.ufcg.psoftarena.Model.State.Subimissao;


import br.edu.ufcg.psoftarena.Model.Submissao;

public class Submetida implements EstadoSubimissao{

    @Override
    public void proximoEstado(Submissao submissao) {
        submissao.setEstadoSubimissao(new EmFila());
    }

    @Override
    public String getCodigo() { return "SUBMETIDA";}
}
