package br.edu.ufcg.psoftarena.Model.State.Submissao;

import br.edu.ufcg.psoftarena.Model.Submissao;

public interface EstadoSubmissao {

    default boolean enfileirar(Submissao submissao) {
        return false;
    }

    default boolean processar(Submissao submissao) {
        return false;
    }

    default boolean concluir(Submissao submissao) {
        return false;
    }

    default boolean falhar(Submissao submissao) {
        return false;
    }

    String getCodigo();
}
