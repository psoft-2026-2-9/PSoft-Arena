package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.ProblemaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.ProblemaResponseDto;
import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProblemaPadraoService implements ProblemaService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ProblemaRepository problemaRepository;

    @Override
    public ProblemaResponseDto addProblema(ProblemaPostPutDto problemaPostPutDto) {
        Problema problema = modelMapper.map(problemaPostPutDto, Problema.class);
        problema.setStatus(StatusProblema.ATIVO);
        return modelMapper.map(problemaRepository.save(problema), ProblemaResponseDto.class);
    }

    @Override
    public List<ProblemaResponseDto> getAllProblemas() {
        return problemaRepository.findAll()
                .stream()
                .map(problema -> modelMapper.map(problema, ProblemaResponseDto.class))
                .toList();
    }

    @Override
    public ProblemaResponseDto getProblemaById(Long id) {
        return modelMapper.map(getExistingProblema(id), ProblemaResponseDto.class);
    }

    @Override
    public ProblemaResponseDto updateProblema(Long id, ProblemaPostPutDto problemaPostPutDto) {
        Problema problema = getExistingProblema(id);
        problema.setTitulo(problemaPostPutDto.getTitulo());
        problema.setDescricao(problemaPostPutDto.getDescricao());
        problema.setRequisitosFuncionais(problemaPostPutDto.getRequisitosFuncionais());
        problema.setRequisitosDeProjeto(problemaPostPutDto.getRequisitosDeProjeto());
        return modelMapper.map(problemaRepository.save(problema), ProblemaResponseDto.class);
    }

    @Override
    public void deleteProblema(Long id) {
        problemaRepository.delete(getExistingProblema(id));
    }

    private Problema getExistingProblema(Long id) {
        return problemaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problema não encontrado"));
    }
}
