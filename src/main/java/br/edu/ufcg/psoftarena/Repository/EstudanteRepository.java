package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.Estudante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudanteRepository extends JpaRepository<Estudante, Long> {
}