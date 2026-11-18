package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.Estudante;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EstudanteIdentidadeTests {

    @Test
    void dadosEditaveisNaoAlteramIdentidade() {
        Estudante estudante = Estudante.builder().id(1L)
                .nome("Ana").matricula("123").email("ana@ufcg.edu.br").build();
        Set<Estudante> estudantes = new HashSet<>();
        estudantes.add(estudante);
        int hashAnterior = estudante.hashCode();

        estudante.setNome("Ana Silva");
        estudante.setMatricula("456");
        estudante.setEmail("ana.silva@ufcg.edu.br");

        assertEquals(hashAnterior, estudante.hashCode());
        assertTrue(estudantes.contains(estudante));
        assertTrue(estudantes.remove(estudante));
    }

    @Test
    void mesmoIdRepresentaMesmoEstudanteMesmoComDadosDiferentes() {
        Estudante original = Estudante.builder().id(1L).nome("Ana").build();
        Estudante atualizado = Estudante.builder().id(1L).nome("Ana Silva").build();

        assertEquals(original, atualizado);
        assertEquals(atualizado, original);
        assertEquals(original.hashCode(), atualizado.hashCode());
        Set<Estudante> estudantes = new HashSet<>();
        estudantes.add(original);
        assertFalse(estudantes.add(atualizado));
    }

    @Test
    void idsDiferentesRepresentamEstudantesDiferentes() {
        assertNotEquals(Estudante.builder().id(1L).nome("Ana").build(),
                Estudante.builder().id(2L).nome("Ana").build());
    }

    @Test
    void estudantesSemIdSoSaoIguaisAPropriaInstancia() {
        Estudante primeiro = Estudante.builder().nome("Ana").build();
        Estudante segundo = Estudante.builder().nome("Ana").build();

        assertEquals(primeiro, primeiro);
        assertNotEquals(primeiro, segundo);
        assertNotEquals(primeiro, null);
        assertNotEquals(primeiro, "Ana");
        Set<Estudante> estudantes = new HashSet<>();
        estudantes.add(primeiro);
        estudantes.add(segundo);
        assertEquals(2, estudantes.size());
    }
}
