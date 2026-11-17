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
public class SubmissaoPostDto {

    @JsonProperty("estudante_id")
    @NotNull(message = "Id do estudante não pode ser nulo")
    private Long estudanteId;

    @JsonProperty("codigo_fonte")
    @NotBlank(message = "Código-fonte não pode ser vazio")
    private String codigoFonte;
}
