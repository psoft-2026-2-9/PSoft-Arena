package br.edu.ufcg.psoftarena.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstudantePostPutDto {
    @JsonProperty("nome")
    @NotBlank(message = "Nome do estudante não pode ser vazio")
    private String nome;
    @JsonProperty("matricula")
    @NotBlank(message = "Matrícula do estudante não pode ser vazia")
    private String matricula;
    @JsonProperty("email")
    @NotBlank(message = "E-mail do estudante não pode ser vazio")
    @Email(message = "E-mail do estudante deve ser válido")
    private String email;
}