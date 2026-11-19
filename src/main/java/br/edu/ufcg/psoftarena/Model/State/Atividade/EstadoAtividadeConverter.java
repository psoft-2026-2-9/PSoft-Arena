package br.edu.ufcg.psoftarena.Model.State.Atividade;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter
public class EstadoAtividadeConverter implements AttributeConverter<EstadoAtividade, String> {

    private static final Map<String, EstadoAtividade> ESTADOS = Stream.of(
                    new AtividadeAberta(), new AtividadeConcluida())
            .collect(Collectors.toMap(EstadoAtividade::getCodigo, Function.identity()));

    @Override
    public String convertToDatabaseColumn(EstadoAtividade estado) {
        return estado == null ? null : estado.getCodigo();
    }

    @Override
    public EstadoAtividade convertToEntityAttribute(String codigo) {
        if (codigo == null) {
            return null;
        }

        EstadoAtividade estado = ESTADOS.get(codigo);
        if (estado == null) {
            throw new IllegalArgumentException("Código de estado inválido: " + codigo);
        }
        return estado;
    }
}
