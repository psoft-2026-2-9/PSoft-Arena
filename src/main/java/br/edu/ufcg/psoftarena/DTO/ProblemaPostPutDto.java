package br.edu.ufcg.psoftarena.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Column(nullable = false)
    private List<String> requisitosFuncionais;

    @JsonProperty("requisitos_de_projeto")
    @Column(nullable = false)
    private List<String> requisitosDeProjeto;
}
