package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Exception.RecursoNaoEncontradoException;

import br.edu.ufcg.psoftarena.DTO.EstudantePostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstudanteResponseDto;
import br.edu.ufcg.psoftarena.Exception.RegraNegocioException;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstudanteCrudPadraoService implements EstudanteCrudService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EstudanteRepository estudanteRepository;

    @Autowired
    TurmaRepository turmaRepository;

    @Autowired
    SubmissaoRepository submissaoRepository;

    @Override
    public EstudanteResponseDto criarEstudante(EstudantePostPutDto estudantePostPutDto) {
        if (estudanteRepository.existsByMatricula(estudantePostPutDto.getMatricula())) {
            throw new RegraNegocioException("Matrícula de estudante já cadastrada");
        }
        Estudante estudante = modelMapper.map(estudantePostPutDto, Estudante.class);
        return modelMapper.map(salvarEstudante(estudante), EstudanteResponseDto.class);
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
            throw new RegraNegocioException("Matrícula de estudante já cadastrada");
        }
        modelMapper.map(estudantePostPutDto, estudante);
        return modelMapper.map(salvarEstudante(estudante), EstudanteResponseDto.class);
    }

    @Override
    public void removerEstudante(Long id) {
        Estudante estudante = getExistingEstudante(id);
        if (turmaRepository.existsByEstudantes_Id(id)) {
            throw new RegraNegocioException("Estudante está associado a uma ou mais turmas");
        }
        if (submissaoRepository.existsByEstudanteId(id)) {
            throw new RegraNegocioException("Estudante possui submissões registradas");
        }
        try {
            estudanteRepository.delete(estudante);
            estudanteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException(
                    "Não foi possível remover o estudante devido a uma restrição de integridade", e);
        }
    }

    private Estudante salvarEstudante(Estudante estudante) {
        try {
            return estudanteRepository.saveAndFlush(estudante);
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException(
                    "Não foi possível salvar o estudante devido a uma restrição de integridade", e);
        }
    }

    private Estudante getExistingEstudante(Long id) {
        return estudanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estudante não encontrado"));
    }
}
