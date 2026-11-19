package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Exception.RegraNegocioException;
import br.edu.ufcg.psoftarena.Exception.RecursoNaoEncontradoException;

import br.edu.ufcg.psoftarena.DTO.ProblemaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.ProblemaResponseDto;
import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemaPadraoService implements ProblemaService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ProblemaRepository problemaRepository;

    @Autowired
    AtividadeRepository atividadeRepository;

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
        // não pode ser deletado caso tenha um problema associado
        Problema problema = getExistingProblema(id);
        if (atividadeRepository.existsByProblemaId(id)) {
            throw new RegraNegocioException("Problema associado a uma atividade não pode ser removido");
        }
        problemaRepository.delete(problema);
    }

    @Override
    public ProblemaResponseDto inativarProblema(Long id) {
        Problema problema = getExistingProblema(id);
        problema.setStatus(StatusProblema.INATIVO);

        return modelMapper.map(problemaRepository.save(problema), ProblemaResponseDto.class);
    }

    private Problema getExistingProblema(Long id) {
        return problemaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Problema não encontrado"));
    }
}
