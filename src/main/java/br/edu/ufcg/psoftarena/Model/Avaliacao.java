package br.edu.ufcg.psoftarena.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Builder
@Data
@Table(name = "tb_avaliacoes")
@AllArgsConstructor
@NoArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "submissao_id", nullable = false, unique = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Submissao submissao;

    @Column(nullable = false)
    private Double nota;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_avaliacoes_evidencias", joinColumns = @JoinColumn(name = "avaliacao_id"))
    @Column(name = "evidencia", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> evidencias = new ArrayList<>();

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    public static Avaliacao compor(Submissao submissao, List<ResultadoModelo> resultados) {
        double nota = resultados.stream()
                .mapToDouble(ResultadoModelo::getNota)
                .average()
                .orElseThrow();

        return Avaliacao.builder()
                .submissao(submissao)
                .nota(Math.round(nota * 100) / 100.0)
                .justificativa(resultados.stream()
                        .map(ResultadoModelo::getJustificativa)
                        .collect(Collectors.joining("\n\n")))
                .evidencias(resultados.stream()
                        .flatMap(resultado -> resultado.getEvidencias().stream())
                        .collect(Collectors.toCollection(ArrayList::new)))
                .dataHora(LocalDateTime.now())
                .build();
    }
}
