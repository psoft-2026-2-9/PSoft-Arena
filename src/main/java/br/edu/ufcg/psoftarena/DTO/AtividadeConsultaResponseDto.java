package br.edu.ufcg.psoftarena.DTO;

import br.edu.ufcg.psoftarena.ENUM.StatusAtividade;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AtividadeConsultaResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("titulo")
    private String titulo;

    @JsonProperty("turma")
    private TurmaResumoDto turma;

    @JsonProperty("problema")
    private ProblemaResponseDto problema;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("estrategia_avaliacao")
    private EstrategiaResumoDto estrategiaAvaliacao;

    @JsonProperty("prazo")
    private LocalDateTime prazo;

    @JsonProperty("status")
    private StatusAtividade status;
}
