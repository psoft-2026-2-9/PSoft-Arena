package br.edu.ufcg.psoftarena.Model.State.Subimissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public interface EstadoSubimissao {
    void proximoEstado(Submissao submissao);

    default void falhar(Submissao submissao) {}

    String getCodigo();
}
