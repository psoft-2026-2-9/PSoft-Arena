package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.TurmaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.TurmaResponseDto;

import java.util.List;

public interface TurmaCrudService {

    TurmaResponseDto addTurma(TurmaPostPutDto turmaPostPutDto);

    List<TurmaResponseDto> getAllTurmas(Long professorId);

    TurmaResponseDto getTurmaById(Long id);

    TurmaResponseDto updateTurma(Long id, TurmaPostPutDto turmaPostPutDto);

    TurmaResponseDto encerrarTurma(Long id);

    void validarTurmaAtiva(Long id);
}
