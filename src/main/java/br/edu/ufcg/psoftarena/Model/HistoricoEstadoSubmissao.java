package br.edu.ufcg.psoftarena.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_historico_estados_submissoes")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoEstadoSubmissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submissao_id", nullable = false, updatable = false)
    private Submissao submissao;

    @Column(name = "estado_anterior", updatable = false)
    private String estadoAnterior;

    @Column(name = "estado_novo", nullable = false, updatable = false)
    private String estadoNovo;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;
}
