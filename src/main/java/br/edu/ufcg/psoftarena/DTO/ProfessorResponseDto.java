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
public class ProfessorResponseDto {

    @JsonProperty("id")
    @NotNull(message = "Id do professor não pode ser nulo")
    private Long id;

    @JsonProperty("nome")
    @NotBlank(message = "Nome do professor não pode ser vazio")
    private String nome;
}
