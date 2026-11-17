package br.edu.ufcg.psoftarena.Model;

import br.edu.ufcg.psoftarena.ENUM.StatusSubmissao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@Table(
        name = "tb_submissoes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"atividade_id", "estudante_id", "versao"})
)
@AllArgsConstructor
@NoArgsConstructor
public class Submissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estudante_id", nullable = false)
    private Estudante estudante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "atividade_id", nullable = false)
    private Atividade atividade;

    @Column(nullable = false)
    private int versao;

    @Column(name = "data_hora_submissao", nullable = false)
    private LocalDateTime dataHoraSubmissao;

    @Lob
    @Column(nullable = false)
    private String codigoFonte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSubmissao status;
}
