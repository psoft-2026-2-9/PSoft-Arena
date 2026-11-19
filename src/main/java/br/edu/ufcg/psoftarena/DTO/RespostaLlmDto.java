package br.edu.ufcg.psoftarena.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RespostaLlmDto {

    private Double nota;

    private String justificativa;

    private List<String> evidencias;
}
