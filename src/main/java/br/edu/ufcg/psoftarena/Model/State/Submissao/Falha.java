package br.edu.ufcg.psoftarena.Model.State.Submissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class Falha implements EstadoSubmissao{
    @Override
    public void proximoEstado(Submissao submissao) {}

    @Override
    public String getCodigo() { return "FALHA";}
}
