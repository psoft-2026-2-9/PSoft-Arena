package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.SubmissaoPostDto;
import br.edu.ufcg.psoftarena.DTO.SubmissaoResponseDto;
import br.edu.ufcg.psoftarena.ENUM.StatusSubmissao;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class SubmissaoCrudPadraoService implements SubmissaoCrudService {

    @Autowired
    SubmissaoRepository submissaoRepository;

    @Autowired
    AtividadeRepository atividadeRepository;

    @Autowired
    EstudanteRepository estudanteRepository;

    @Autowired
    TurmaCrudService turmaCrudService;

    @Override
    @Transactional
    public SubmissaoResponseDto submeterSolucao(Long atividadeId, SubmissaoPostDto submissaoPostDto) {
        Atividade atividade = getExistingAtividade(atividadeId);
        if (!atividade.aceitaSubmissao()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Atividade concluída não aceita novas submissões");
        }
        if (LocalDateTime.now().isAfter(atividade.getPrazo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prazo da atividade expirado");
        }

        Estudante estudante = getExistingEstudante(submissaoPostDto.getEstudanteId());
        turmaCrudService.validarEstudantePertenceTurma(atividade.getTurma().getId(), estudante.getId());

        int versao = submissaoRepository
                .findFirstByAtividadeIdAndEstudanteIdOrderByVersaoDesc(atividade.getId(), estudante.getId())
                .map(submissao -> submissao.getVersao() + 1)
                .orElse(1);

        Submissao submissao = Submissao.builder()
                .estudante(estudante)
                .atividade(atividade)
                .versao(versao)
                .dataHoraSubmissao(LocalDateTime.now())
                .codigoFonte(submissaoPostDto.getCodigoFonte())
                .status(StatusSubmissao.SUBMETIDA)
                .build();

        return toResponse(salvarSubmissao(submissao));
    }

    private Submissao salvarSubmissao(Submissao submissao) {
        try {
            return submissaoRepository.saveAndFlush(submissao);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Submissão concorrente registrada, envie novamente", e);
        }
    }

    private Atividade getExistingAtividade(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada"));
    }

    private Estudante getExistingEstudante(Long id) {
        return estudanteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado"));
    }

    private SubmissaoResponseDto toResponse(Submissao submissao) {
        return SubmissaoResponseDto.builder()
                .id(submissao.getId())
                .estudanteId(submissao.getEstudante().getId())
                .atividadeId(submissao.getAtividade().getId())
                .versao(submissao.getVersao())
                .dataHoraSubmissao(submissao.getDataHoraSubmissao())
                .status(submissao.getStatus())
                .build();
    }
}
