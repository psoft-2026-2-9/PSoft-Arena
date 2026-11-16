package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.AtividadePostDto;
import br.edu.ufcg.psoftarena.DTO.AtividadeResponseDto;
import br.edu.ufcg.psoftarena.ENUM.StatusAtividade;
import br.edu.ufcg.psoftarena.ENUM.StatusProblema;
import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Model.Turma;
import br.edu.ufcg.psoftarena.Repository.AtividadeRepository;
import br.edu.ufcg.psoftarena.Repository.EstrategiaRepository;
import br.edu.ufcg.psoftarena.Repository.ProblemaRepository;
import br.edu.ufcg.psoftarena.Repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Override
    @Transactional
    public AtividadeResponseDto criarAtividade(AtividadePostDto atividadePostDto) {
        Turma turma = getExistingTurma(atividadePostDto.getTurmaId());
        if (turma.getStatus() != StatusTurma.ATIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Turma encerrada não pode receber novas atividades");
        }

        Problema problema = getExistingProblema(atividadePostDto.getProblemaId());
        if (problema.getStatus() != StatusProblema.ATIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Problema inativo não pode ser usado em atividades");
        }

        Estrategia estrategia = getExistingEstrategia(atividadePostDto.getEstrategiaId());

        Atividade atividade = Atividade.builder()
                .titulo(atividadePostDto.getTitulo())
                .descricao(atividadePostDto.getDescricao())
                .prazo(atividadePostDto.getPrazo())
                .status(StatusAtividade.ABERTA)
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
        if (atividade.getStatus() == StatusAtividade.CONCLUIDA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Atividade já está concluída");
        }

        atividade.setStatus(StatusAtividade.CONCLUIDA);
        return toResponse(atividadeRepository.save(atividade));
    }

    private Turma getExistingTurma(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));
    }

    private Problema getExistingProblema(Long id) {
        return problemaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problema não encontrado"));
    }

    private Estrategia getExistingEstrategia(Long id) {
        return estrategiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estratégia não encontrada"));
    }

    private Atividade getExistingAtividade(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada"));
    }

    private AtividadeResponseDto toResponse(Atividade atividade) {
        return AtividadeResponseDto.builder()
                .id(atividade.getId())
                .titulo(atividade.getTitulo())
                .descricao(atividade.getDescricao())
                .prazo(atividade.getPrazo())
                .status(atividade.getStatus())
                .turmaId(atividade.getTurma().getId())
                .problemaId(atividade.getProblema().getId())
                .estrategiaId(atividade.getEstrategia().getId())
                .llmModelo(atividade.getLlmModelo())
                .llmUrlApi(atividade.getLlmUrlApi())
                .build();
    }
}
