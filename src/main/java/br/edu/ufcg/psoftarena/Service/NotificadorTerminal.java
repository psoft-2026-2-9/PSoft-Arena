package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.Estudante;
import org.springframework.stereotype.Component;

@Component
public class NotificadorTerminal implements NotificadorEstudante {

    @Override
    public void notificar(Estudante estudante, String mensagem) {
        System.out.println("Notificação ao estudante " + estudante.getId() + ": " + mensagem);
    }
}
