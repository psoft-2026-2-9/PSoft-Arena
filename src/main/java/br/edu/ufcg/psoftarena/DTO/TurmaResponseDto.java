package br.edu.ufcg.psoftarena.DTO;

import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TurmaResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("periodo_letivo")
    private String periodoLetivo;

    @JsonProperty("professor")
    private ProfessorResponseDto professor;

    @JsonProperty("status")
    private StatusTurma status;
}
