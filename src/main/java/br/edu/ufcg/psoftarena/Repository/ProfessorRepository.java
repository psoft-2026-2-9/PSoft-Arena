package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}
