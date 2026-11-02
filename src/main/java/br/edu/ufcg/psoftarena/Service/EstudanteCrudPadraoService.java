package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.EstudantePostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstudanteResponseDto;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstudanteCrudPadraoService implements EstudanteCrudService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EstudanteRepository estudanteRepository;

    @Override
    public EstudanteResponseDto criarEstudante(EstudantePostPutDto estudantePostPutDto) {
        Estudante estudante = modelMapper.map(estudantePostPutDto, Estudante.class);
        return modelMapper.map(estudanteRepository.save(estudante), EstudanteResponseDto.class);
    }

    @Override
    public List<EstudanteResponseDto> buscarTodosEstudantes() {
        return estudanteRepository.findAll()
                .stream()
                .map(estudante -> modelMapper.map(estudante, EstudanteResponseDto.class))
                .toList();
    }

    @Override
    public EstudanteResponseDto buscarEstudante(Long id) {
        Estudante estudante = estudanteRepository.findById(id).orElseThrow();
        return modelMapper.map(estudante, EstudanteResponseDto.class);
    }

    @Override
    public EstudanteResponseDto editarEstudante(Long id, EstudantePostPutDto estudantePostPutDto) {
        Estudante estudante = estudanteRepository.findById(id).orElseThrow();
        modelMapper.map(estudantePostPutDto, estudante);
        return modelMapper.map(estudanteRepository.save(estudante), EstudanteResponseDto.class);
    }
}