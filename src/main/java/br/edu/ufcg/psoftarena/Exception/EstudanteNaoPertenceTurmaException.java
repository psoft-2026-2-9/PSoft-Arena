package br.edu.ufcg.psoftarena.Exception;

public class EstudanteNaoPertenceTurmaException extends RuntimeException {

    public EstudanteNaoPertenceTurmaException() {
        super("Estudante não pertence à turma");
    }
}
