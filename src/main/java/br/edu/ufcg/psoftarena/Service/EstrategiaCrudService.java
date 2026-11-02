package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.EstrategiaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstrategiaResponseDto;
import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;

import java.util.List;

public interface EstrategiaCrudService {

    EstrategiaResponseDto criarEstrategia(EstrategiaPostPutDto estrategiaPostPutDto);

    List<EstrategiaResponseDto> buscarTodasEstrategias(TipoEstrategia tipo);

    EstrategiaResponseDto buscarEstrategia(Long id);

    EstrategiaResponseDto editarEstrategia(Long id, EstrategiaPostPutDto estrategiaPostPutDto);
}
