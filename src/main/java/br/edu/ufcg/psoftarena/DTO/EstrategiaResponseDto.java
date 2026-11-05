package br.edu.ufcg.psoftarena.DTO;

import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstrategiaResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("tipo")
    private TipoEstrategia tipo;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("prompt")
    private String prompt;
}
