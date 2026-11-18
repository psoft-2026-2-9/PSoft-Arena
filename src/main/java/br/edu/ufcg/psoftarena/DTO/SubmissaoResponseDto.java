package br.edu.ufcg.psoftarena.DTO;

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
public class SubmissaoResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("estudante_id")
    private Long estudanteId;

    @JsonProperty("atividade_id")
    private Long atividadeId;

    @JsonProperty("versao")
    private int versao;

    @JsonProperty("data_hora_submissao")
    private LocalDateTime dataHoraSubmissao;

    @JsonProperty("status")
    private String status;
}
