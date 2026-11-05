package br.edu.ufcg.psoftarena.DTO;

import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstrategiaPostPutDto {

    @JsonProperty("nome")
    @NotBlank(message = "Nome da estratégia não pode ser vazio")
    private String nome;

    @JsonProperty("tipo")
    @NotNull(message = "Tipo da estratégia não pode ser nulo")
    private TipoEstrategia tipo;

    @JsonProperty("descricao")
    @NotBlank(message = "Descrição da estratégia não pode ser vazia")
    private String descricao;

    @JsonProperty("prompt")
    @NotBlank(message = "Prompt da estratégia não pode ser vazio")
    private String prompt;
}
