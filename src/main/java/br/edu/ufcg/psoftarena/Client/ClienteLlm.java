package br.edu.ufcg.psoftarena.Client;

public interface ClienteLlm {

    String enviarPrompt(ConfiguracaoLlm configuracao, String prompt);
}
