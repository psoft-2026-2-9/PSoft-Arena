package br.edu.ufcg.psoftarena.Model.State.Submissao;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EstadoSubmissaoConverter implements AttributeConverter<EstadoSubmissao, String> {

    @Override
    public String convertToDatabaseColumn(EstadoSubmissao estado) {
        return estado == null ? null : estado.getCodigo();
    }

    @Override
    public EstadoSubmissao convertToEntityAttribute(String codigo) {
        if (codigo == null) {
            return null;
        }

        return switch (codigo) {
            case "SUBMETIDA" -> new Submetida();
            case "EMFILA" -> new EmFila();
            case "PROCESSANDO" -> new Processando();
            case "CONCLUIDA" -> new Concluida();
            case "FALHA" -> new Falha();
            default -> throw new IllegalArgumentException("Código de estado inválido: " + codigo);
        };
    }
}
