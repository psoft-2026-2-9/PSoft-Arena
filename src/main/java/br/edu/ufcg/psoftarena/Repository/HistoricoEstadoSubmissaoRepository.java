package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.HistoricoEstadoSubmissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoEstadoSubmissaoRepository extends JpaRepository<HistoricoEstadoSubmissao, Long> {

    List<HistoricoEstadoSubmissao> findBySubmissaoIdOrderByDataHoraAscIdAsc(Long submissaoId);
}
