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
public class AtividadeResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("titulo")
    private String titulo;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("prazo")
    private LocalDateTime prazo;

    @JsonProperty("status")
    private String status;

    @JsonProperty("turma_id")
    private Long turmaId;

    @JsonProperty("problema_id")
    private Long problemaId;

    @JsonProperty("estrategia_id")
    private Long estrategiaId;

    @JsonProperty("llm_modelo")
    private String llmModelo;

    @JsonProperty("llm_url_api")
    private String llmUrlApi;
}
