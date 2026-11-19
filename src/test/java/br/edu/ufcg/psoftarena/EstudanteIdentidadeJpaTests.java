package br.edu.ufcg.psoftarena;

import br.edu.ufcg.psoftarena.Model.Estudante;
import br.edu.ufcg.psoftarena.Repository.EstudanteRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EstudanteIdentidadeJpaTests {

    @Autowired
    EstudanteRepository estudanteRepository;

    @Autowired
    EntityManager entityManager;

    private Estudante novoEstudante() {
        return Estudante.builder().nome("Ana").matricula("identidade-123")
                .email("ana@ufcg.edu.br").build();
    }

    @Test
    void gerarIdNaoQuebraColecaoQueJaContemEstudante() {
        Estudante estudante = novoEstudante();
        Set<Estudante> estudantes = new HashSet<>();
        estudantes.add(estudante);
        int hashAnterior = estudante.hashCode();

        estudanteRepository.saveAndFlush(estudante);

        assertNotNull(estudante.getId());
        assertEquals(hashAnterior, estudante.hashCode());
        assertTrue(estudantes.contains(estudante));
        assertTrue(estudantes.remove(estudante));
    }

    @Test
    void entidadeEReferenciaJpaTemAMesmaIdentidade() {
        Estudante estudante = estudanteRepository.saveAndFlush(novoEstudante());
        entityManager.clear();
        Estudante referencia = estudanteRepository.getReferenceById(estudante.getId());

        assertFalse(Hibernate.isInitialized(referencia));
        assertEquals(estudante, referencia);
        assertEquals(referencia, estudante);
        assertEquals(estudante.hashCode(), referencia.hashCode());
        Set<Estudante> estudantes = new HashSet<>();
        estudantes.add(estudante);
        assertTrue(estudantes.contains(referencia));
        assertTrue(estudantes.remove(referencia));
        assertFalse(Hibernate.isInitialized(referencia));
    }
}
