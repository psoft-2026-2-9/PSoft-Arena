package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.State.Subimissao.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EstadoSubmissaoConverterTests {

    private final EstadoSubimissaoConverter converter = new EstadoSubimissaoConverter();

    @Test
    void preservaValoresNulosNosDoisSentidos() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void rejeitaCodigoDesconhecido() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute("DESCONHECIDO"));
    }

    @Test
    void converteEstadosValidosNosDoisSentidos() {
        var tipos = Map.of(
                "SUBMETIDA", Submetida.class,
                "EMFILA", EmFila.class,
                "PROCESSANDO", Processando.class,
                "CONCLUIDA", Concluida.class,
                "FALHA", Falha.class);
        tipos.forEach((codigo, tipo) -> {
            var estado = converter.convertToEntityAttribute(codigo);
            assertInstanceOf(tipo, estado);
            assertEquals(codigo, converter.convertToDatabaseColumn(estado));
        });
    }
}
