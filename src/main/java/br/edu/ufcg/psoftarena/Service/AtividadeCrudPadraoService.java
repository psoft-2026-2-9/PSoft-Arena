package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Exception.EstudanteNaoPertenceTurmaException;
import br.edu.ufcg.psoftarena.Exception.RecursoNaoEncontradoException;
import br.edu.ufcg.psoftarena.Exception.RegraNegocioException;

import br.edu.ufcg.psoftarena.DTO.AtividadePostDto;
import br.edu.ufcg.psoftarena.DTO.AtividadeResponseDto;
import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeAberta;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.EstrategiaRepository;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.ufcg.psoftarena.DTO.AtividadeConsultaResponseDto;
import br.edu.ufcg.psoftarena.DTO.EstrategiaResumoDto;
import br.edu.ufcg.psoftarena.DTO.ProblemaResponseDto;
import br.edu.ufcg.psoftarena.DTO.TurmaResumoDto;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import java.util.List;


@Service
public class AtividadeCrudPadraoService implements AtividadeCrudService {

    @Autowired
    AtividadeRepository atividadeRepository;

    @Autowired
    TurmaRepository turmaRepository;

    @Autowired
    ProblemaRepository problemaRepository;

    @Autowired
    EstrategiaRepository estrategiaRepository;

    @Autowired
    TextEncryptor textEncryptor;

    @Autowired
    EstudanteRepository estudanteRepository;

    @Override
    @Transactional
    public AtividadeResponseDto criarAtividade(AtividadePostDto atividadePostDto) {
        Turma turma = getExistingTurma(atividadePostDto.getTurmaId());
        if (turma.getStatus() != StatusTurma.ATIVA) {
            throw new RegraNegocioException("Turma encerrada não pode receber novas atividades");
        }

        Problema problema = getExistingProblema(atividadePostDto.getProblemaId());
        if (problema.getStatus() != StatusProblema.ATIVO) {
            throw new RegraNegocioException("Problema inativo não pode ser usado em atividades");
        }

        Estrategia estrategia = getExistingEstrategia(atividadePostDto.getEstrategiaId());

        Atividade atividade = Atividade.builder()
                .titulo(atividadePostDto.getTitulo())
                .descricao(atividadePostDto.getDescricao())
                .prazo(atividadePostDto.getPrazo())
                .status(new AtividadeAberta())
                .turma(turma)
                .problema(problema)
                .estrategia(estrategia)
                .llmModelo(atividadePostDto.getLlmModelo())
                .llmUrlApi(atividadePostDto.getLlmUrlApi())
                .llmChaveApiCriptografada(textEncryptor.encrypt(atividadePostDto.getLlmChaveApi()))
                .build();

        return toResponse(atividadeRepository.save(atividade));
    }

    @Override
    @Transactional
    public AtividadeResponseDto concluirAtividade(Long id) {
        Atividade atividade = getExistingAtividade(id);
        if (!atividade.concluir()) {
            throw new RegraNegocioException("Atividade já está concluída");
        }

        return toResponse(atividadeRepository.save(atividade));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtividadeConsultaResponseDto> listarAtividadesDoEstudante(Long estudanteId) {
        validarEstudanteExiste(estudanteId);

        return atividadeRepository.findAllByEstudanteId(estudanteId)
                .stream()
                .map(this::toConsultaResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AtividadeConsultaResponseDto consultarAtividadeDoEstudante(Long atividadeId, Long estudanteId) {
        validarEstudanteExiste(estudanteId);

        Atividade atividade = atividadeRepository.findByIdWithDetails(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada"));

        if (!turmaRepository.existsByIdAndEstudantes_Id(atividade.getTurma().getId(), estudanteId)) {
            throw new EstudanteNaoPertenceTurmaException();
        }

        return toConsultaResponse(atividade);
    }

    private Turma getExistingTurma(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada"));
    }

    private Problema getExistingProblema(Long id) {
        return problemaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Problema não encontrado"));
    }

    private Estrategia getExistingEstrategia(Long id) {
        return estrategiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estratégia não encontrada"));
    }

    private Atividade getExistingAtividade(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada"));
    }

    private void validarEstudanteExiste(Long estudanteId) {
        if (!estudanteRepository.existsById(estudanteId)) {
            throw new RecursoNaoEncontradoException("Estudante não encontrado");
        }
    }

    private AtividadeResponseDto toResponse(Atividade atividade) {
        return AtividadeResponseDto.builder()
                .id(atividade.getId())
                .titulo(atividade.getTitulo())
                .descricao(atividade.getDescricao())
                .prazo(atividade.getPrazo())
                .status(atividade.getStatus().getCodigo())
                .turmaId(atividade.getTurma().getId())
                .problemaId(atividade.getProblema().getId())
                .estrategiaId(atividade.getEstrategia().getId())
                .llmModelo(atividade.getLlmModelo())
                .llmUrlApi(atividade.getLlmUrlApi())
                .build();
    }

    private AtividadeConsultaResponseDto toConsultaResponse(Atividade atividade) {
        return AtividadeConsultaResponseDto.builder()
                .id(atividade.getId())
                .titulo(atividade.getTitulo())
                .turma(toTurmaResumo(atividade.getTurma()))
                .problema(toProblemaResponse(atividade.getProblema()))
                .descricao(atividade.getDescricao())
                .estrategiaAvaliacao(toEstrategiaResumo(atividade.getEstrategia()))
                .prazo(atividade.getPrazo())
                .status(atividade.getStatus().getCodigo())
                .build();
    }

    private TurmaResumoDto toTurmaResumo(Turma turma) {
        return TurmaResumoDto.builder()
                .id(turma.getId())
                .nome(turma.getNome())
                .codigo(turma.getCodigo())
                .periodoLetivo(turma.getPeriodoLetivo())
                .status(turma.getStatus())
                .build();
    }

    private ProblemaResponseDto toProblemaResponse(Problema problema) {
        return ProblemaResponseDto.builder()
                .id(problema.getId())
                .titulo(problema.getTitulo())
                .descricao(problema.getDescricao())
                .requisitosFuncionais(problema.getRequisitosFuncionais())
                .requisitosDeProjeto(problema.getRequisitosDeProjeto())
                .status(problema.getStatus())
                .build();
    }

    private EstrategiaResumoDto toEstrategiaResumo(Estrategia estrategia) {
        return EstrategiaResumoDto.builder()
                .id(estrategia.getId())
                .nome(estrategia.getNome())
                .tipo(estrategia.getTipo())
                .descricao(estrategia.getDescricao())
                .build();
    }
}
