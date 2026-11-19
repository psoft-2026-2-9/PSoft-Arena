package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MontadorPromptAvaliacaoTests {

    @Test
    void representaRequisitosAusentesSemInterromperMontagem() {
        Problema problema = Problema.builder().titulo("Problema").descricao("Descrição")
                .requisitosFuncionais(null).requisitosDeProjeto(List.of()).build();
        Estrategia estrategia = Estrategia.builder().nome("Coesão").prompt("Avalie a coesão").build();
        Submissao submissao = Submissao.builder().codigoFonte("class Main {}")
                .atividade(Atividade.builder().problema(problema).estrategia(estrategia).build()).build();

        String prompt = new MontadorPromptAvaliacao().montar(submissao);

        assertTrue(prompt.contains("Requisitos funcionais:\n- Nenhum"));
        assertTrue(prompt.contains("Requisitos de projeto:\n- Nenhum"));
        assertTrue(prompt.contains("Avalie a coesão"));
        assertTrue(prompt.contains("class Main {}"));
    }
}
