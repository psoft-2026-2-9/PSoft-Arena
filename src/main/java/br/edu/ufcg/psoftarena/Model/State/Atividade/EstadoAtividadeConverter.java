package br.edu.ufcg.psoftarena.Model.State.Atividade;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EstadoAtividadeConverter implements AttributeConverter<EstadoAtividade, String> {

    @Override
    public String convertToDatabaseColumn(EstadoAtividade estado) {
        return estado == null ? null : estado.getCodigo();
    }

    @Override
    public EstadoAtividade convertToEntityAttribute(String codigo) {
        if (codigo == null) {
            return null;
        }

        return switch (codigo) {
            case "ABERTA" -> new AtividadeAberta();
            case "CONCLUIDA" -> new AtividadeConcluida();
            default -> throw new IllegalArgumentException("Código de estado inválido: " + codigo);
        };
    }
}
