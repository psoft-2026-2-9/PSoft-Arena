package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.ResultadoModelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoModeloRepository extends JpaRepository<ResultadoModelo, Long> {

    List<ResultadoModelo> findBySubmissaoIdOrderByIdAsc(Long submissaoId);
}
