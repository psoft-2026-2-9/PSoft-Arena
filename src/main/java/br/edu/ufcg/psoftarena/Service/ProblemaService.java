package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.ProblemaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.ProblemaResponseDto;

import java.util.List;

public interface ProblemaService {

    ProblemaResponseDto addProblema(ProblemaPostPutDto problemaPostPutDto);

    List<ProblemaResponseDto> getAllProblemas();

    ProblemaResponseDto getProblemaById(Long id);

    ProblemaResponseDto updateProblema(Long id, ProblemaPostPutDto problemaPostPutDto);

    void deleteProblema(Long id);
}
