package br.edu.ufcg.psoftarena.DTO;

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
public class TurmaPostPutDto {

    @JsonProperty("nome")
    @NotBlank(message = "Nome da turma não pode ser vazio")
    private String nome;

    @JsonProperty("codigo")
    @NotBlank(message = "Código da turma não pode ser vazio")
    private String codigo;

    @JsonProperty("periodo_letivo")
    @NotBlank(message = "Período letivo da turma não pode ser vazio")
    private String periodoLetivo;

    @JsonProperty("professor_id")
    @NotNull(message = "Id do professor não pode ser nulo")
    private Long professorId;
}
