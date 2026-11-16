package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    boolean existsByProblemaId(Long problemaId);
}
