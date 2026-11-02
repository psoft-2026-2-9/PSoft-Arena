package br.edu.ufcg.psoftarena.Model;

import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@Table(name = "tb_estrategias")
@AllArgsConstructor
@NoArgsConstructor
public class Estrategia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @Column(nullable = false)
    @JsonProperty("nome")
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty("tipo")
    private TipoEstrategia tipo;

    @Column(nullable = false) // varchar(255), atencao
    @JsonProperty("descricao")
    private String descricao;

    @Column(nullable = false, columnDefinition = "TEXT")
    @JsonProperty("prompt")
    private String prompt;
}
