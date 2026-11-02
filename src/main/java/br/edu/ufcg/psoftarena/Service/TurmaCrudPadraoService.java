package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.TurmaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.TurmaResponseDto;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.Model.Professor;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.ProfessorRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TurmaCrudPadraoService implements TurmaCrudService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TurmaRepository turmaRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Override
    public TurmaResponseDto addTurma(TurmaPostPutDto turmaPostPutDto) {
        if (turmaRepository.existsByCodigo(turmaPostPutDto.getCodigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de turma já cadastrado");
        }
        Professor professor = getExistingProfessor(turmaPostPutDto.getProfessorId());
        Turma turma = Turma.builder()
                .nome(turmaPostPutDto.getNome())
                .codigo(turmaPostPutDto.getCodigo())
                .periodoLetivo(turmaPostPutDto.getPeriodoLetivo())
                .professor(professor)
                .status(StatusTurma.ATIVA)
                .build();
        return modelMapper.map(turmaRepository.save(turma), TurmaResponseDto.class);
    }

    @Override
    public List<TurmaResponseDto> getAllTurmas(Long professorId) {
        List<Turma> turmas = professorId == null
                ? turmaRepository.findAll()
                : turmaRepository.findAllByProfessorId(professorId);
        return turmas.stream()
                .map(turma -> modelMapper.map(turma, TurmaResponseDto.class))
                .toList();
    }

    @Override
    public TurmaResponseDto getTurmaById(Long id) {
        return modelMapper.map(getExistingTurma(id), TurmaResponseDto.class);
    }

    @Override
    @Transactional
    public TurmaResponseDto updateTurma(Long id, TurmaPostPutDto turmaPostPutDto) {
        Turma turma = getExistingTurma(id);
        if (turma.getStatus() == StatusTurma.ENCERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Turma encerrada não pode ser editada");
        }
        if (turmaRepository.existsByCodigoAndIdNot(turmaPostPutDto.getCodigo(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de turma já cadastrado");
        }
        
        Professor professor = getExistingProfessor(turmaPostPutDto.getProfessorId());

        turma.setNome(turmaPostPutDto.getNome());
        turma.setCodigo(turmaPostPutDto.getCodigo());
        turma.setPeriodoLetivo(turmaPostPutDto.getPeriodoLetivo());
        turma.setProfessor(professor);
        return modelMapper.map(turmaRepository.save(turma), TurmaResponseDto.class);
    }

    @Override
    public TurmaResponseDto encerrarTurma(Long id) {
        Turma turma = getExistingTurma(id);
        if (turma.getStatus() == StatusTurma.ENCERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Turma já está encerrada");
        }
        turma.setStatus(StatusTurma.ENCERRADA);
        return modelMapper.map(turmaRepository.save(turma), TurmaResponseDto.class);
    }

    @Override
    public void validarTurmaAtiva(Long id) {
        Turma turma = getExistingTurma(id);
        if (turma.getStatus() == StatusTurma.ENCERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Turma encerrada não pode receber novas associações ou atividades");
        }
    }

    private Turma getExistingTurma(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));
    }

    private Professor getExistingProfessor(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
    }
}
