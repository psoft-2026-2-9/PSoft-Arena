package br.edu.ufcg.psoftarena.Model.State.Submissao;


import br.edu.ufcg.psoftarena.Model.Submissao;

public class Submetida implements EstadoSubmissao{

    @Override
    public void proximoEstado(Submissao submissao) {
        submissao.setEstadoSubmissao(new EmFila());
    }

    @Override
    public String getCodigo() { return "SUBMETIDA";}
}
