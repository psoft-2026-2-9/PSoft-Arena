package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.FilaSubmissoes;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class FilaSubmissoesTests {

    @Test
    void forneceSempreAMesmaInstancia() {
        assertSame(FilaSubmissoes.getInstance(), FilaSubmissoes.getInstance());
    }

    @Test
    void retiraSubmissoesNaOrdemDeInclusao() throws ReflectiveOperationException {
        // Isola os itens dos consumidores reais mantidos pelos contextos Spring dos outros testes.
        var construtor = FilaSubmissoes.class.getDeclaredConstructor();
        construtor.setAccessible(true);
        FilaSubmissoes fila = construtor.newInstance();

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            fila.adicionar(30L);
            fila.adicionar(10L);
            fila.adicionar(20L);

            assertEquals(30L, fila.retirar());
            assertEquals(10L, fila.retirar());
            assertEquals(20L, fila.retirar());
        });
    }
}
