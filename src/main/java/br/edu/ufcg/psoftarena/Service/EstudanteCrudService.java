package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.EstudantePostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstudanteResponseDto;

import java.util.List;

public interface EstudanteCrudService {
    EstudanteResponseDto criarEstudante(EstudantePostPutDto estudantePostPutDto);

    List<EstudanteResponseDto> buscarTodosEstudantes();

    EstudanteResponseDto buscarEstudante(Long id);

    EstudanteResponseDto editarEstudante(Long id, EstudantePostPutDto estudantePostPutDto);

    void removerEstudante(Long id);
}