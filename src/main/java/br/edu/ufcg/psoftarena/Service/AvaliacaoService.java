package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.ResultadoAvaliacaoResponseDto;

public interface AvaliacaoService {

    void avaliarSubmissao(Long submissaoId);

    ResultadoAvaliacaoResponseDto consultarResultado(
            Long atividadeId, Long submissaoId, Long estudanteId);
}
