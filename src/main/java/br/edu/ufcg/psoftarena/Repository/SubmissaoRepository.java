package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.Submissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubmissaoRepository extends JpaRepository<Submissao, Long> {

    Optional<Submissao> findFirstByAtividadeIdAndEstudanteIdOrderByVersaoDesc(Long atividadeId, Long estudanteId);

    boolean existsByEstudanteId(Long estudanteId);
}
