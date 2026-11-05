package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.ProfessorPostPutDto;
import br.edu.ufcg.psoftarena.DTO.ProfessorResponseDto;
import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProfessorCrudPadraoService implements ProfessorCrudService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ProfessorRepository professorRepository;

    @Override
    public ProfessorResponseDto addProfessor(ProfessorPostPutDto professorPostPutDto) {
        Professor professor = modelMapper.map(professorPostPutDto, Professor.class);
        return modelMapper.map(professorRepository.save(professor), ProfessorResponseDto.class);
    }

    @Override
    public List<ProfessorResponseDto> getAllProfessors() {
        return professorRepository.findAll()
                .stream()
                .map(professor -> modelMapper.map(professor, ProfessorResponseDto.class))
                .toList();
    }

    @Override
    public ProfessorResponseDto getProfessorById(Long id) {
        return modelMapper.map(getExistingProfessor(id), ProfessorResponseDto.class);
    }

    @Override
    public ProfessorResponseDto updateProfessor(Long id, ProfessorPostPutDto professorPostPutDto) {
        Professor professor = getExistingProfessor(id);
        professor.setNome(professorPostPutDto.getNome());
        return modelMapper.map(professorRepository.save(professor), ProfessorResponseDto.class);
    }

    @Override
    public void deleteProfessor(Long id) {
        professorRepository.delete(getExistingProfessor(id));
    }

    private Professor getExistingProfessor(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
    }
}
