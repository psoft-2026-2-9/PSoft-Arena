package br.edu.ufcg.psoftarena.Model;

import br.edu.ufcg.psoftarena.Model.State.Subimissao.EstadoSubimissao;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.EstadoSubimissaoConverter;
import br.edu.ufcg.psoftarena.Model.State.Subimissao.Submetida;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    @Convert(converter = EstadoSubimissaoConverter.class)
    @Column(nullable = false)
    @Builder.Default
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EstadoSubimissao estadoSubimissao = new Submetida();

    public void proximoEstado() {
        estadoSubimissao.proximoEstado(this);
    }

    public void falhar() {
        estadoSubimissao.falhar(this);
    }

    public void setEstadoSubmissao(EstadoSubimissao estadoSubmissao) { this.estadoSubimissao = estadoSubmissao; }
}
