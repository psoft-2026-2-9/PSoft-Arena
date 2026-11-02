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
import br.edu.ufcg.psoftarena.DTO.EstudanteResponseDto;
import br.edu.ufcg.psoftarena.DTO.ProfessorResponseDto;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;

import java.util.List;

@Service
public class TurmaCrudPadraoService implements TurmaCrudService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TurmaRepository turmaRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    EstudanteRepository estudanteRepository;

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
        return  toResponse(turmaRepository.save(turma));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurmaResponseDto> getAllTurmas(Long professorId) {
        List<Turma> turmas = professorId == null
                ? turmaRepository.findAll()
                : turmaRepository.findAllByProfessorId(professorId);
           
        return turmas.stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TurmaResponseDto getTurmaById(Long id) {
        return toResponse(getExistingTurma(id));
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
        return toResponse(turmaRepository.save(turma));
    }

    @Override
    @Transactional 
    public TurmaResponseDto encerrarTurma(Long id) {
        Turma turma = getExistingTurma(id);
        if (turma.getStatus() == StatusTurma.ENCERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Turma já está encerrada");
        }
        turma.setStatus(StatusTurma.ENCERRADA);
        return toResponse(turmaRepository.save(turma));
    }

    @Override
    public void validarTurmaAtiva(Long id) {
        Turma turma = getExistingTurma(id);
        if (turma.getStatus() == StatusTurma.ENCERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Turma encerrada não pode receber novas associações ou atividades");
        }
    }

    @Override 
    @Transactional
    public TurmaResponseDto adicionarEstudante(Long turmaId, Long estudanteId) {
        Turma turma = getExistingTurma(turmaId);

        if (turma.getStatus() == StatusTurma.ENCERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Turma encerrada não pode receber novas associações");
        }

        Estudante estudante = getExistingEstudante(estudanteId);

        if (estudantePertenceTurma(turma, estudanteId)){
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Estudante já associado a esta turma");
        }

        turma.getEstudantes().add(estudante);
        return toResponse(turmaRepository.save(turma));

    }   

    @Override
    @Transactional(readOnly = true)
    public void validarEstudantePertenceTurma(Long turmaId, Long estudanteId) {
        Turma turma = getExistingTurma(turmaId);

        if (!estudantePertenceTurma(turma, estudanteId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Estudante não pertence à turma");
        }
    }

    @Override 
    @Transactional
    public void removerEstudante(Long turmaId, Long estudanteId) {
        Turma turma = getExistingTurma(turmaId);
        getExistingEstudante(estudanteId);

        if (!podeRemoverEstudante(turma, estudanteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Associação entre estudante e turma não encontrada");
        }

        turmaRepository.save(turma);
    }

    //metodo auxiliar
    private boolean estudantePertenceTurma(Turma turma, Long estudanteId) {
        for (Estudante estudante : turma.getEstudantes()) {
            if (estudante.getId().equals(estudanteId)) {
                return true;
            }
        }

        return false;
    }

    //metodo auxiliar
    private boolean podeRemoverEstudante(Turma turma, Long estudanteId) {
        return turma.getEstudantes().removeIf(e -> e.getId().equals(estudanteId));
    }

    private Turma getExistingTurma(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));
    }

    private Professor getExistingProfessor(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
    }

    private Estudante getExistingEstudante(Long id) {
        return estudanteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estudante não encontrado"));
    }

    private TurmaResponseDto toResponse(Turma turma) {
        TurmaResponseDto dto = new TurmaResponseDto();
        dto.setId(turma.getId());
        dto.setNome(turma.getNome());
        dto.setCodigo(turma.getCodigo());
        dto.setPeriodoLetivo(turma.getPeriodoLetivo());
        dto.setStatus(turma.getStatus());
        dto.setProfessor(turma.getProfessor() == null
                ? null
                : modelMapper.map(turma.getProfessor(), ProfessorResponseDto.class));
        dto.setEstudantes(turma.getEstudantes().stream()
                .map(estudante -> modelMapper.map(estudante, EstudanteResponseDto.class))
                .toList());
        return dto;
    }

}
