package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.Submissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SubmissaoRepository extends JpaRepository<Submissao, Long> {

    Optional<Submissao> findFirstByAtividadeIdAndEstudanteIdOrderByVersaoDesc(Long atividadeId, Long estudanteId);

    boolean existsByEstudanteId(Long estudanteId);

    List<Submissao> findByAtividadeIdAndEstudanteIdOrderByVersaoDesc(Long atividadeId, Long estudanteId);

    Optional<Submissao> findByIdAndAtividadeIdAndEstudanteId(Long id, Long atividadeId, Long estudanteId);
}
