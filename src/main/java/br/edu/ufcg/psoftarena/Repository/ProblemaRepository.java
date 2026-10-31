package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.Problema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemaRepository extends JpaRepository<Problema, Long> {
}
