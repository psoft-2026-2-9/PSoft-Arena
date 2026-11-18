package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.State.Subimissao.Concluida;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.Submetida;
import br.edu.ufcg.psoftarena.Model.Submissao;
import br.edu.ufcg.psoftarena.Repository.HistoricoEstadoSubmissaoRepository;
import br.edu.ufcg.psoftarena.Repository.SubmissaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class EstadoSubmissaoServiceTests {

    @Test
    void operacoesSemMudancaNaoPersistemNemNotificam(CapturedOutput output) {
        EstadoSubmissaoPadraoService service = new EstadoSubmissaoPadraoService();
        service.submissaoRepository = mock(SubmissaoRepository.class);
        service.historicoEstadoSubmissaoRepository = mock(HistoricoEstadoSubmissaoRepository.class);
        var concluida = new Concluida();
        Submissao finalizada = Submissao.builder().estadoSubimissao(concluida).build();
        var submetida = new Submetida();
        Submissao nova = Submissao.builder().estadoSubimissao(submetida).build();

        assertFalse(service.avancar(finalizada));
        assertFalse(service.falhar(finalizada));
        assertFalse(service.falhar(nova));

        assertSame(concluida, finalizada.getEstadoSubimissao());
        assertSame(submetida, nova.getEstadoSubimissao());
        verifyNoInteractions(service.submissaoRepository, service.historicoEstadoSubmissaoRepository);
        assertFalse(output.getAll().contains("Notificação ao estudante"));
    }
}
