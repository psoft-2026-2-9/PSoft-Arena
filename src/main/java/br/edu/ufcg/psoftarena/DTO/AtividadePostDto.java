package br.edu.ufcg.psoftarena.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AtividadePostDto {

    @JsonProperty("titulo")
    @NotBlank(message = "Título da atividade não pode ser vazio")
    private String titulo;

    @JsonProperty("descricao")
    @NotBlank(message = "Descrição da atividade não pode ser vazia")
    private String descricao;

    @JsonProperty("prazo")
    @NotNull(message = "Prazo da atividade não pode ser nulo")
    private LocalDateTime prazo;

    @JsonProperty("turma_id")
    @NotNull(message = "Id da turma não pode ser nulo")
    private Long turmaId;

    @JsonProperty("problema_id")
    @NotNull(message = "Id do problema não pode ser nulo")
    private Long problemaId;

    @JsonProperty("estrategia_id")
    @NotNull(message = "Id da estratégia não pode ser nulo")
    private Long estrategiaId;

    @JsonProperty("llm_modelo")
    @NotBlank(message = "Modelo da LLM não pode ser vazio")
    private String llmModelo;

    @JsonProperty("llm_url_api")
    @NotBlank(message = "URL da API da LLM não pode ser vazia")
    private String llmUrlApi;

    @JsonProperty("llm_chave_api")
    @NotBlank(message = "Chave da API da LLM não pode ser vazia")
    private String llmChaveApi;
}
