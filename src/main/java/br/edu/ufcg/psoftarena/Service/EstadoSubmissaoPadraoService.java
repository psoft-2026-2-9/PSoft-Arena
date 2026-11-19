package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.HistoricoEstadoSubmissao;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Repository.HistoricoEstadoSubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.BooleanSupplier;

@Service
public class EstadoSubmissaoPadraoService implements EstadoSubmissaoService {

    @Autowired
    SubmissaoRepository submissaoRepository;

    @Autowired
    HistoricoEstadoSubmissaoRepository historicoEstadoSubmissaoRepository;

    @Autowired
    NotificadorEstudante notificadorEstudante;

    @Override
    @Transactional
    public void registrarCriacao(Submissao submissao) {
        registrarEstado(submissao, null, submissao.getDataHoraSubmissao());
    }

    @Override
    @Transactional
    public boolean enfileirar(Submissao submissao) {
        return mudarEstado(submissao, submissao::enfileirar);
    }

    @Override
    @Transactional
    public boolean iniciarProcessamento(Submissao submissao) {
        return mudarEstado(submissao, submissao::processar);
    }

    @Override
    @Transactional
    public boolean concluir(Submissao submissao) {
        return mudarEstado(submissao, submissao::concluir);
    }

    @Override
    @Transactional
    public boolean falhar(Submissao submissao) {
        return mudarEstado(submissao, submissao::falhar);
    }

    private boolean mudarEstado(Submissao submissao, BooleanSupplier transicao) {
        String estadoAnterior = submissao.getEstadoSubmissao().getCodigo();
        if (!transicao.getAsBoolean()) {
            return false;
        }

        submissaoRepository.saveAndFlush(submissao);
        registrarEstado(submissao, estadoAnterior, LocalDateTime.now());
        return true;
    }

    private void registrarEstado(Submissao submissao, String estadoAnterior, LocalDateTime dataHora) {
        String estadoNovo = submissao.getEstadoSubmissao().getCodigo();
        historicoEstadoSubmissaoRepository.save(HistoricoEstadoSubmissao.builder()
                .submissao(submissao)
                .estadoAnterior(estadoAnterior)
                .estadoNovo(estadoNovo)
                .dataHora(dataHora)
                .build());

        String mudanca = estadoAnterior == null
                ? "registrada no estado " + estadoNovo
                : estadoAnterior + " -> " + estadoNovo;
        notificadorEstudante.notificar(submissao.getEstudante(), "submissão " + submissao.getId() + " - " + mudanca);
    }
}
