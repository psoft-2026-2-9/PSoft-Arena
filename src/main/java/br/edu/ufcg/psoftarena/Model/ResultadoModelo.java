package br.edu.ufcg.psoftarena.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Data
@Table(name = "tb_resultados_modelos")
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoModelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "submissao_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Submissao submissao;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private boolean sucesso;

    private Double nota;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_resultados_modelos_evidencias", joinColumns = @JoinColumn(name = "resultado_id"))
    @Column(name = "evidencia", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> evidencias = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String erro;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;
}
