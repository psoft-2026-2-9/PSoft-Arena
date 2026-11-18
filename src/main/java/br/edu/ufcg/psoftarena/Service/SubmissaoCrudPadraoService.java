package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.SubmissaoPostDto;
import br.edu.ufcg.psoftarena.DTO.SubmissaoResponseDto;
import br.edu.ufcg.psoftarena.Exception.RecursoNaoEncontradoException;
import br.edu.ufcg.psoftarena.Exception.RegraNegocioException;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.Submetida;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissaoCrudPadraoService implements SubmissaoCrudService {

    @Autowired
    SubmissaoRepository submissaoRepository;

    @Autowired
    EstadoSubmissaoService estadoSubmissaoService;

    @Autowired
    EnfileiradorSubmissoes enfileiradorSubmissoes;

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
            throw new RegraNegocioException("Atividade concluída não aceita novas submissões");
        }
        if (LocalDateTime.now().isAfter(atividade.getPrazo())) {
            throw new RegraNegocioException("Prazo da atividade expirado");
        }

        Estudante estudante = validarEstudanteDaAtividade(atividade, submissaoPostDto.getEstudanteId());

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
                .build();

        submissao = salvarSubmissao(submissao);
        estadoSubmissaoService.registrarCriacao(submissao);
        return encaminharParaFila(submissao);
    }




    @Override
    @Transactional(readOnly = true)
    public List<SubmissaoResponseDto> listarSubmissoes(Long atividadeId, Long estudanteId) {
        Atividade atividade = getExistingAtividade(atividadeId);
        validarEstudanteDaAtividade(atividade, estudanteId);
        return submissaoRepository.findByAtividadeIdAndEstudanteIdOrderByVersaoDesc(atividadeId, estudanteId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissaoResponseDto consultarSubmissao(Long atividadeId, Long submissaoId, Long estudanteId) {
        Atividade atividade = getExistingAtividade(atividadeId);
        validarEstudanteDaAtividade(atividade, estudanteId);
        Submissao submissao = submissaoRepository
                .findByIdAndAtividadeIdAndEstudanteId(submissaoId, atividadeId, estudanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Submissão não encontrada"));
        return toResponse(submissao);
    }

    private SubmissaoResponseDto encaminharParaFila(Submissao submissao) {
        if (!(submissao.getEstadoSubimissao() instanceof Submetida)) {
            throw new RegraNegocioException(
                    "Somente submissões no estado Submetida podem ser encaminhadas para a fila");
        }

        if (!estadoSubmissaoService.avancar(submissao)) {
            return toResponse(submissao);
        }

        enfileiradorSubmissoes.enfileirarAposCommit(submissao.getId());

        return toResponse(submissao);
    }

    private Submissao salvarSubmissao(Submissao submissao) {
        try {
            return submissaoRepository.saveAndFlush(submissao);
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException(
                    "Não foi possível salvar a submissão devido a uma restrição de integridade", e);
        }
    }

    private Atividade getExistingAtividade(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada"));
    }

    private Estudante validarEstudanteDaAtividade(Atividade atividade, Long estudanteId) {
        Estudante estudante = getExistingEstudante(estudanteId);
        turmaCrudService.validarEstudantePertenceTurma(atividade.getTurma().getId(), estudante.getId());
        return estudante;
    }

    private Estudante getExistingEstudante(Long id) {
        return estudanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estudante não encontrado"));
    }

    private SubmissaoResponseDto toResponse(Submissao submissao) {
        return SubmissaoResponseDto.builder()
                .id(submissao.getId())
                .estudanteId(submissao.getEstudante().getId())
                .atividadeId(submissao.getAtividade().getId())
                .versao(submissao.getVersao())
                .dataHoraSubmissao(submissao.getDataHoraSubmissao())
                .status(submissao.getEstadoSubimissao().getCodigo())
                .build();
    }
}
