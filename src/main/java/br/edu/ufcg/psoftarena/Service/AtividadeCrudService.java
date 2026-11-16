package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.AtividadeConsultaResponseDto;
import br.edu.ufcg.psoftarena.DTO.AtividadePostDto;
import br.edu.ufcg.psoftarena.DTO.AtividadeResponseDto;
import java.util.List;

public interface AtividadeCrudService {

    AtividadeResponseDto criarAtividade(AtividadePostDto atividadePostDto);

    AtividadeResponseDto concluirAtividade(Long id);

    List<AtividadeConsultaResponseDto> listarAtividadesDoEstudante(Long estudanteId);

    AtividadeConsultaResponseDto consultarAtividadeDoEstudante(Long atividadeId, Long estudanteId)
}
