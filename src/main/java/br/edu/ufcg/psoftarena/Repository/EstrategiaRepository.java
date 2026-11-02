package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstrategiaRepository extends JpaRepository<Estrategia, Long> {

    List<Estrategia> findAllByTipo(TipoEstrategia tipo);
}
