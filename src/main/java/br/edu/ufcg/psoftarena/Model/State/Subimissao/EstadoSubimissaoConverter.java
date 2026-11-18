package br.edu.ufcg.psoftarena.Model.State.Subimissao;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EstadoSubimissaoConverter implements AttributeConverter<EstadoSubimissao, String> {

    @Override
    public String convertToDatabaseColumn(EstadoSubimissao estado) {
        return estado == null ? null : estado.getCodigo();
    }

    @Override
    public EstadoSubimissao convertToEntityAttribute(String codigo) {
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
