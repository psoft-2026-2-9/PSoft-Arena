package br.edu.ufcg.psoftarena.Model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class FilaSubmissoes {
    private static final FilaSubmissoes INSTANCE = new FilaSubmissoes();

    private final BlockingQueue<Long> fila;

    private FilaSubmissoes() {
        fila = new LinkedBlockingQueue<>();
    }

    public static FilaSubmissoes getInstance() {
        return INSTANCE;
    }

    public void adicionar(Long submissaoId) {
        fila.add(submissaoId);
    }

    public Long retirar() throws InterruptedException {
        return fila.take();
    }
}
