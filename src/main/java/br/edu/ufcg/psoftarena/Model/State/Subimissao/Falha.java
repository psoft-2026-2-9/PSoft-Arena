package br.edu.ufcg.psoftarena.Model.State.Subimissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class Falha implements EstadoSubimissao{
    @Override
    public void proximoEstado(Submissao submissao) {}

    @Override
    public String getCodigo() { return "FALHA";}
}
