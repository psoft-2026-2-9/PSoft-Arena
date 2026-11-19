package br.edu.ufcg.psoftarena.Model.State.Submissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public interface EstadoSubmissao {
    void proximoEstado(Submissao submissao);

    default void falhar(Submissao submissao) {}

    String getCodigo();
}
