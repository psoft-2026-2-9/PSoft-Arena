package br.edu.ufcg.psoftarena.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@Table(name = "tb_estudante")
@AllArgsConstructor
@NoArgsConstructor
public class Estudante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @Column(name = "nome", nullable = false)
    @JsonProperty("nome")
    private String nome;

    @Column(name = "matricula", nullable = false, unique = true)
    @JsonProperty("matricula")
    private String matricula;

    @Column(name = "email", nullable = false)
    @JsonProperty("email")
    private String email;

    @Override
    public final boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Estudante estudante)) {
            return false;
        }
        return getId() != null && getId().equals(estudante.getId());
    }

    @Override
    public final int hashCode() {
        // O hash deve permanecer estável mesmo quando a persistência gerar o ID.
        return Estudante.class.hashCode();
    }
}
