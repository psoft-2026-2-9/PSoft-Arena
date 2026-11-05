package br.edu.ufcg.psoftarena.Model;

import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Builder
@Data
@Table(name = "tb_problemas")
@AllArgsConstructor
@NoArgsConstructor
public class Problema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private long id;

    @JsonProperty("titulo")
    @NotEmpty
    private String titulo;

    @JsonProperty("descricao")
    @NotEmpty
    private String descricao;

    @JsonProperty("requisitos_funcionais")
    @NotEmpty
    private List<String> requisitosFuncionais;

    @JsonProperty("requisitos_de_projeto")
    @NotNull
    private List<String> requisitosDeProjeto;

    @JsonProperty("status")
    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusProblema status;
}
