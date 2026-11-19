package br.edu.ufcg.psoftarena.Model;

import br.edu.ufcg.psoftarena.Model.State.Submissao.EstadoSubmissao;
import br.edu.ufcg.psoftarena.Model.State.Submissao.EstadoSubmissaoConverter;
import br.edu.ufcg.psoftarena.Model.State.Submissao.Submetida;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

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
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Estudante estudante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "atividade_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Atividade atividade;

    @Column(nullable = false)
    private int versao;

    @Column(name = "data_hora_submissao", nullable = false)
    private LocalDateTime dataHoraSubmissao;

    @Lob
    @Column(nullable = false)
    @ToString.Exclude
    private String codigoFonte;

    @Convert(converter = EstadoSubmissaoConverter.class)
    @Column(nullable = false)
    @Builder.Default
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NonNull
    @Setter(AccessLevel.NONE)
    private EstadoSubmissao estadoSubmissao = new Submetida();

    public boolean enfileirar() {
        return estadoSubmissao.enfileirar(this);
    }

    public boolean processar() {
        return estadoSubmissao.processar(this);
    }

    public boolean concluir() {
        return estadoSubmissao.concluir(this);
    }

    public boolean falhar() {
        return estadoSubmissao.falhar(this);
    }

    public void mudarEstado(EstadoSubmissao novoEstado) {
        this.estadoSubmissao = Objects.requireNonNull(novoEstado, "Estado da submissão não pode ser nulo");
    }
}
