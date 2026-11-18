package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.Model.Atividade;
import br.edu.ufcg.psoftarena.Model.Problema;
import br.edu.ufcg.psoftarena.Model.Submissao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MontadorPromptAvaliacao {

    private static final String MODELO_PROMPT = """
            Você é um avaliador de soluções de problemas de projeto de software orientado a objetos.
            Avalie a solução em Java abaixo apenas por análise estática do código-fonte, sem executá-la.

            Critérios da estratégia de avaliação "%s":
            %s

            Problema: %s
            %s

            Requisitos funcionais:
            %s

            Requisitos de projeto:
            %s

            Código-fonte submetido:
            ```java
            %s
            ```

            Responda somente com um objeto JSON, sem texto adicional, no formato:
            {"nota": <número de 0 a 10>, "justificativa": "<justificativa da nota>", "evidencias": ["<trecho ou observação do código que sustenta a avaliação>"]}
            """;

    public String montar(Submissao submissao) {
        Atividade atividade = submissao.getAtividade();
        Problema problema = atividade.getProblema();

        return MODELO_PROMPT.formatted(
                atividade.getEstrategia().getNome(),
                atividade.getEstrategia().getPrompt(),
                problema.getTitulo(),
                problema.getDescricao(),
                listar(problema.getRequisitosFuncionais()),
                listar(problema.getRequisitosDeProjeto()),
                submissao.getCodigoFonte());
    }

    private String listar(List<String> itens) {
        if (itens == null || itens.isEmpty()) {
            return "- Nenhum";
        }
        return itens.stream().map(item -> "- " + item).collect(Collectors.joining("\n"));
    }
}
