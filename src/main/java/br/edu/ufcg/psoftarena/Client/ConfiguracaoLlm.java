package br.edu.ufcg.psoftarena.Client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ConfiguracaoLlm {

    private final String modelo;

    private final String urlApi;

    private final String chaveApi;
}
