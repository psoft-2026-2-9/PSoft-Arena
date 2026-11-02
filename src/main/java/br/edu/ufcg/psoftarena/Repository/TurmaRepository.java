package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    List<Turma> findAllByProfessorId(Long professorId);
}
