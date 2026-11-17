package br.edu.ufcg.psoftarena.Repository;

import br.edu.ufcg.psoftarena.Model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    boolean existsByProblemaId(Long problemaId);

    @Query("""
            select a from Atividade a
            join fetch a.turma
            join fetch a.problema
            join fetch a.estrategia
            where a.turma.id in (
                select t.id from Turma t join t.estudantes e where e.id = :estudanteId
            )
            order by a.prazo asc
            """)
    List<Atividade> findAllByEstudanteId(@Param("estudanteId") Long estudanteId);

    @Query("""
            select a from Atividade a
            join fetch a.turma
            join fetch a.problema
            join fetch a.estrategia
            where a.id = :id
            """)
    Optional<Atividade> findByIdWithDetails(@Param("id") Long id);
}
