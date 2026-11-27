package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Client.ClienteLlm;
import br.edu.ufcg.psoftarena.Client.ConfiguracaoLlm;
import br.edu.ufcg.psoftarena.DTO.ResultadoAvaliacaoResponseDto;
import br.edu.ufcg.psoftarena.DTO.RespostaLlmDto;
import br.edu.ufcg.psoftarena.DTO.SubmissaoResponseDto;
import br.edu.ufcg.psoftarena.Exception.FalhaAvaliacaoException;
import br.edu.ufcg.psoftarena.Exception.RecursoNaoEncontradoException;
import br.edu.ufcg.psoftarena.Exception.RegraNegocioException;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Avaliacao;
import br.edu.ufcg.psoftarena.Model.ResultadoModelo;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Repository.AvaliacaoRepository;
import br.edu.ufcg.psoftarena.Repository.ResultadoModeloRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AvaliacaoPadraoService implements AvaliacaoService {

    @Autowired
    SubmissaoRepository submissaoRepository;

    @Autowired
    AvaliacaoRepository avaliacaoRepository;

    @Autowired
    ResultadoModeloRepository resultadoModeloRepository;

    @Autowired
    EstadoSubmissaoService estadoSubmissaoService;

    @Autowired
    ClienteLlm clienteLlm;

    @Autowired
    MontadorPromptAvaliacao montadorPromptAvaliacao;

    @Autowired
    InterpretadorRespostaLlm interpretadorRespostaLlm;

    @Autowired
    TextEncryptor textEncryptor;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    SubmissaoCrudService submissaoCrudService;

    @Autowired
    NotificadorEstudante notificadorEstudante;

    @Override
    public void avaliarSubmissao(Long submissaoId) {
        Submissao submissao = getExistingSubmissao(submissaoId);
        if (!estadoSubmissaoService.iniciarProcessamento(submissao)) {
            throw new RegraNegocioException("Somente submissões no estado Em fila podem ser avaliadas");
        }

        try {
            List<ResultadoModelo> resultados = avaliarComModelos(submissao);
            transactionTemplate.executeWithoutResult(status -> registrarResultado(submissao, resultados));
        } catch (RuntimeException e) {
            log.error("Falha inesperada ao avaliar a submissão {}", submissaoId, e);
            estadoSubmissaoService.falhar(getExistingSubmissao(submissaoId));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoAvaliacaoResponseDto consultarResultado(
            Long atividadeId, Long submissaoId, Long estudanteId) {
        SubmissaoResponseDto submissao = submissaoCrudService
                .consultarSubmissao(atividadeId, submissaoId, estudanteId);
        String situacao = submissao.getStatus();

        if ("FALHA".equals(situacao)) {
            List<ResultadoModelo> resultados = resultadoModeloRepository
                    .findBySubmissaoIdOrderByIdAsc(submissaoId);
            return ResultadoAvaliacaoResponseDto.builder()
                    .submissaoId(submissaoId)
                    .evidencias(new ArrayList<>())
                    .situacao(situacao)
                    .dataHoraAvaliacao(resultados.stream()
                            .map(ResultadoModelo::getDataHora)
                            .filter(Objects::nonNull)
                            .max(LocalDateTime::compareTo)
                            .orElse(null))
                    .erros(resultados.stream()
                            .map(ResultadoModelo::getErro)
                            .filter(Objects::nonNull)
                            .toList())
                    .build();
        }

        if (!"CONCLUIDA".equals(situacao)) {
            throw new RegraNegocioException("Avaliação ainda não concluída");
        }

        Avaliacao avaliacao = avaliacaoRepository.findBySubmissaoId(submissaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Resultado da avaliação não encontrado"));

        return ResultadoAvaliacaoResponseDto.builder()
                .submissaoId(submissaoId)
                .nota(avaliacao.getNota())
                .justificativa(avaliacao.getJustificativa())
                .evidencias(new ArrayList<>(avaliacao.getEvidencias()))
                .situacao(situacao)
                .dataHoraAvaliacao(avaliacao.getDataHora())
                .erros(new ArrayList<>())
                .build();
    }

    private List<ResultadoModelo> avaliarComModelos(Submissao submissao) {
        String prompt = montadorPromptAvaliacao.montar(submissao);
        return modelosDaAtividade(submissao.getAtividade()).stream()
                .map(modelo -> avaliarComModelo(submissao, modelo, prompt))
                .toList();
    }

    private List<ConfiguracaoLlm> modelosDaAtividade(Atividade atividade) {
        return List.of(ConfiguracaoLlm.builder()
                .modelo(atividade.getLlmModelo())
                .urlApi(atividade.getLlmUrlApi())
                .chaveApi(textEncryptor.decrypt(atividade.getLlmChaveApiCriptografada()))
                .build());
    }

    private ResultadoModelo avaliarComModelo(Submissao submissao, ConfiguracaoLlm modelo, String prompt) {
        ResultadoModelo.ResultadoModeloBuilder resultado = ResultadoModelo.builder()
                .submissao(submissao)
                .modelo(modelo.getModelo())
                .dataHora(LocalDateTime.now());

        try {
            RespostaLlmDto resposta = interpretadorRespostaLlm.interpretar(clienteLlm.enviarPrompt(modelo, prompt));
            return resultado
                    .sucesso(true)
                    .nota(resposta.getNota())
                    .justificativa(resposta.getJustificativa())
                    .evidencias(resposta.getEvidencias())
                    .build();
        } catch (FalhaAvaliacaoException e) {
            return resultado
                    .sucesso(false)
                    .erro(e.getMessage())
                    .build();
        }
    }

    private void registrarResultado(Submissao submissao, List<ResultadoModelo> resultados) {
        resultadoModeloRepository.saveAll(resultados);

        if (resultados.stream().allMatch(ResultadoModelo::isSucesso)) {
            avaliacaoRepository.save(Avaliacao.compor(submissao, resultados));
            estadoSubmissaoService.concluir(submissao);
            notificadorEstudante.notificar(submissao.getEstudante(),
                    "resultado da avaliação disponível para a submissão " + submissao.getId());
        } else {
            estadoSubmissaoService.falhar(submissao);
            notificadorEstudante.notificar(submissao.getEstudante(),
                    "resultado da avaliação (falha) disponível para a submissão " + submissao.getId());
        }
    }

    private Submissao getExistingSubmissao(Long id) {
        return submissaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Submissão não encontrada"));
    }
}
