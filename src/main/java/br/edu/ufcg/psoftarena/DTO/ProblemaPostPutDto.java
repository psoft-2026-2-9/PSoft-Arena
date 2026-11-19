package br.edu.ufcg.psoftarena.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemaPostPutDto {

    @JsonProperty("titulo")
    @NotBlank(message = "Titulo do problema não pode ser vazio")
    private String titulo;

    @JsonProperty("descricao")
    @NotBlank(message = "Descrição do problema não pode ser vazia")
    private String descricao;

    @JsonProperty("requisitos_funcionais")
    @NotEmpty(message = "Requisitos funcionais não podem ser vazios")
    private List<@NotBlank(message = "Requisito funcional não pode ser vazio") String> requisitosFuncionais;

    @JsonProperty("requisitos_de_projeto")
    @NotEmpty(message = "Requisitos de projeto não podem ser vazios")
    private List<@NotBlank(message = "Requisito de projeto não pode ser vazio") String> requisitosDeProjeto;
}
