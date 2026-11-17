package br.edu.ufcg.psoftarena.Model.State.Subimissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class Processando implements EstadoSubimissao{

    @Override
    public void proximoEstado(Submissao submissao) {
        // implementar apos a US10
    }

    @Override
    public String getCodigo() { return "PROCESSANDO";}
}
