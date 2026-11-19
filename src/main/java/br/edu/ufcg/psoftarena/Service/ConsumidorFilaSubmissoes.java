package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.FilaSubmissoes;
import br.edu.ufcg.psoftarena.Model.State.Submissao.EmFila;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumidorFilaSubmissoes {

    @Autowired
    AvaliacaoService avaliacaoService;

    @Autowired
    SubmissaoRepository submissaoRepository;

    private Thread consumidor;
    private final FilaSubmissoes fila;

    public ConsumidorFilaSubmissoes() {
        this(FilaSubmissoes.getInstance());
    }

    ConsumidorFilaSubmissoes(FilaSubmissoes fila) {
        this.fila = fila;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void iniciar() {
        submissaoRepository.findAllByEstadoSubmissao(new EmFila())
                .forEach(submissao -> fila.adicionar(submissao.getId()));

        consumidor = new Thread(this::consumir, "consumidor-fila-submissoes");
        consumidor.setDaemon(true);
        consumidor.start();
    }

    @PreDestroy
    public void parar() {
        if (consumidor != null) {
            consumidor.interrupt();
        }
    }

    void processar(Long submissaoId) {
        try {
            avaliacaoService.avaliarSubmissao(submissaoId);
        } catch (RuntimeException e) {
            System.out.println("Erro ao processar a submissão " + submissaoId + ": " + e.getMessage());
        }
    }

    private void consumir() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                processar(fila.retirar());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
