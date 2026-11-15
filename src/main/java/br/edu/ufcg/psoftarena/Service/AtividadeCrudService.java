package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.AtividadePostDto;
import br.edu.ufcg.psoftarena.DTO.AtividadeResponseDto;

public interface AtividadeCrudService {

    AtividadeResponseDto criarAtividade(AtividadePostDto atividadePostDto);

    AtividadeResponseDto concluirAtividade(Long id);
}
