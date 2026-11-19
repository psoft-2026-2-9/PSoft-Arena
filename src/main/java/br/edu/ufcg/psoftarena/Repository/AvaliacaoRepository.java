package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findBySubmissaoId(Long submissaoId);
}
