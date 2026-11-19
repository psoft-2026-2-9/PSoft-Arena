package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeAberta;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeConcluida;
import br.edu.ufcg.psoftarena.Model.State.Atividade.EstadoAtividadeConverter;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EstadoAtividadeConverterTests {

    private final EstadoAtividadeConverter converter = new EstadoAtividadeConverter();

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
        var tipos = Map.of("ABERTA", AtividadeAberta.class, "CONCLUIDA", AtividadeConcluida.class);
        tipos.forEach((codigo, tipo) -> {
            var estado = converter.convertToEntityAttribute(codigo);
            assertInstanceOf(tipo, estado);
            assertEquals(codigo, converter.convertToDatabaseColumn(estado));
        });
    }
}
