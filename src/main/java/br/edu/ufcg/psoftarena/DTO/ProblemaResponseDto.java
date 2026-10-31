package br.edu.ufcg.psoftarena.DTO;


import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemaResponseDto {

    @JsonProperty("id")
    private long id;

    @JsonProperty("titulo")
    private String titulo;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("requisitos_funcionais")
    private List<String> requisitosFuncionais;

    @JsonProperty("requisitos_de_projeto")
    private List<String> requisitosDeProjeto;

    @JsonProperty("status")
    private StatusProblema status;
}
