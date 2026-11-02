package br.edu.ufcg.psoftarena.Model;

import br.edu.ufcg.psoftarena.ENUM.StatusTurma;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Builder
@Data
@Table(name = "tb_turmas")
@AllArgsConstructor
@NoArgsConstructor
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private long id;

    @Column(nullable = false)
    @JsonProperty("nome")
    private String nome;

    @Column(nullable = false, unique = true)
    @JsonProperty("codigo")
    private String codigo;

    @Column(nullable = false)
    @JsonProperty("periodo_letivo")
    private String periodoLetivo;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonProperty("professor")
    private Professor professor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty("status")
    private StatusTurma status;
}
