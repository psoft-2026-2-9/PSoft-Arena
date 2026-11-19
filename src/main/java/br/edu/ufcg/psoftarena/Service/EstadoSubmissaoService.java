package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.Submissao;

public interface EstadoSubmissaoService {

    void registrarCriacao(Submissao submissao);

    boolean enfileirar(Submissao submissao);

    boolean iniciarProcessamento(Submissao submissao);

    boolean concluir(Submissao submissao);

    boolean falhar(Submissao submissao);
}
