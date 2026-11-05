package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.EstudantePostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstudanteResponseDto;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EstudanteCrudPadraoService implements EstudanteCrudService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EstudanteRepository estudanteRepository;

    @Autowired
    TurmaRepository turmaRepository;

    @Override
    public EstudanteResponseDto criarEstudante(EstudantePostPutDto estudantePostPutDto) {
        if (estudanteRepository.existsByMatricula(estudantePostPutDto.getMatricula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula de estudante já cadastrada");
        }
        Estudante estudante = modelMapper.map(estudantePostPutDto, Estudante.class);
        return modelMapper.map(salvarMatriculaUnica(estudante), EstudanteResponseDto.class);
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
        return modelMapper.map(getExistingEstudante(id), EstudanteResponseDto.class);
    }

    @Override
    public EstudanteResponseDto editarEstudante(Long id, EstudantePostPutDto estudantePostPutDto) {
        Estudante estudante = getExistingEstudante(id);
        if (estudanteRepository.existsByMatriculaAndIdNot(estudantePostPutDto.getMatricula(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula de estudante já cadastrada");
        }
        modelMapper.map(estudantePostPutDto, estudante);
        return modelMapper.map(salvarMatriculaUnica(estudante), EstudanteResponseDto.class);
    }

    @Override
    public void removerEstudante(Long id) {
        Estudante estudante = getExistingEstudante(id);
        if (turmaRepository.existsByEstudantes_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Estudante está associado a uma ou mais turmas");
        }
        try {
            estudanteRepository.delete(estudante);
            estudanteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Estudante está associado a uma ou mais turmas", e);
        }
    }

    private Estudante salvarMatriculaUnica(Estudante estudante) {
        try {
            return estudanteRepository.saveAndFlush(estudante);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula de estudante já cadastrada", e);
        }
    }

    private Estudante getExistingEstudante(Long id) {
        return estudanteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado"));
    }
}
