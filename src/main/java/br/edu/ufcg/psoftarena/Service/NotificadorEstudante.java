package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.Estudante;

public interface NotificadorEstudante {

    void notificar(Estudante estudante, String mensagem);
}
