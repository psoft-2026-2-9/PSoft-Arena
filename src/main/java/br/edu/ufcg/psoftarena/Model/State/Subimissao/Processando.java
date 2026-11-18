package br.edu.ufcg.psoftarena.Model.State.Subimissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public class Processando implements EstadoSubimissao{

    @Override
    public void proximoEstado(Submissao submissao) {
        // TODO US10: definir a transição para Concluida ou Falha conforme o resultado da avaliação.
    }

    @Override
    public String getCodigo() { return "PROCESSANDO";}
}
