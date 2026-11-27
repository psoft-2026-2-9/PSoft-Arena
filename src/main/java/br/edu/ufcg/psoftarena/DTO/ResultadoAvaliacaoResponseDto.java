package br.edu.ufcg.psoftarena.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoAvaliacaoResponseDto {

    @JsonProperty("submissao_id")
    private Long submissaoId;

    @JsonProperty("nota")
    private Double nota;

    @JsonProperty("justificativa")
    private String justificativa;

    @JsonProperty("evidencias")
    @Builder.Default
    private List<String> evidencias = new ArrayList<>();

    @JsonProperty("situacao")
    private String situacao;

    @JsonProperty("data_hora_avaliacao")
    private LocalDateTime dataHoraAvaliacao;

    @JsonProperty("erros")
    @Builder.Default
    private List<String> erros = new ArrayList<>();
}
