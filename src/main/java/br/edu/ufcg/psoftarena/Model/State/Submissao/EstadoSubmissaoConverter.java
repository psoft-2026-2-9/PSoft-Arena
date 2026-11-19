package br.edu.ufcg.psoftarena.Model.State.Submissao;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter
public class EstadoSubmissaoConverter implements AttributeConverter<EstadoSubmissao, String> {

    private static final Map<String, EstadoSubmissao> ESTADOS = Stream.of(
                    new Submetida(), new EmFila(), new Processando(), new Concluida(), new Falha())
            .collect(Collectors.toMap(EstadoSubmissao::getCodigo, Function.identity()));

    @Override
    public String convertToDatabaseColumn(EstadoSubmissao estado) {
        return estado == null ? null : estado.getCodigo();
    }

    @Override
    public EstadoSubmissao convertToEntityAttribute(String codigo) {
        if (codigo == null) {
            return null;
        }

        EstadoSubmissao estado = ESTADOS.get(codigo);
        if (estado == null) {
            throw new IllegalArgumentException("Código de estado inválido: " + codigo);
        }
        return estado;
    }
}
