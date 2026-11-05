package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.ProfessorPostPutDto;
import br.edu.ufcg.psoftarena.DTO.ProfessorResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ProfessorCrudService {

    ProfessorResponseDto addProfessor(ProfessorPostPutDto professorPostPutDto);

    List<ProfessorResponseDto> getAllProfessors();

    ProfessorResponseDto getProfessorById(Long id);

    ProfessorResponseDto updateProfessor(Long id, ProfessorPostPutDto professorPostPutDto);

    void deleteProfessor(Long id);
}
