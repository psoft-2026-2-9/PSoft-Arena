package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.TurmaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.TurmaResponseDto;
import br.edu.ufcg.psoftarena.DTO.EstudanteResponseDto;
import java.util.List;

public interface TurmaCrudService {

    TurmaResponseDto addTurma(TurmaPostPutDto turmaPostPutDto);

    List<TurmaResponseDto> getAllTurmas(Long professorId);

    TurmaResponseDto getTurmaById(Long id);

    TurmaResponseDto updateTurma(Long id, TurmaPostPutDto turmaPostPutDto);

    TurmaResponseDto encerrarTurma(Long id);

    void validarTurmaAtiva(Long id);

    TurmaResponseDto adicionarEstudante(Long turmaId, Long estudanteId);
    
    void removerEstudante(Long turmaId, Long estudanteId);

    List<EstudanteResponseDto> listarEstudantes(Long turmaId);

    // "apenas alunos da turma participam das atividades" 
    void validarEstudantePertenceTurma(Long turmaId, Long estudanteId);

}
