package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.SubmissaoPostDto;
import br.edu.ufcg.psoftarena.DTO.SubmissaoResponseDto;
import java.util.List;

public interface SubmissaoCrudService {

    SubmissaoResponseDto submeterSolucao(Long atividadeId, SubmissaoPostDto submissaoPostDto);

    List<SubmissaoResponseDto> listarSubmissoes(Long atividadeId, Long estudanteId);

    SubmissaoResponseDto consultarSubmissao(Long atividadeId, Long submissaoId, Long estudanteId);
}
