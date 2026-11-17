package br.edu.ufcg.psoftarena.Model;

import br.edu.ufcg.psoftarena.Model.State.Atividade.AtividadeAberta;
import br.edu.ufcg.psoftarena.Model.State.Atividade.EstadoAtividade;
import br.edu.ufcg.psoftarena.Model.State.Atividade.EstadoAtividadeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@Table(name = "tb_atividades")
@AllArgsConstructor
@NoArgsConstructor
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime prazo;

    @Convert(converter = EstadoAtividadeConverter.class)
    @Column(nullable = false)
    @Builder.Default
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EstadoAtividade status = new AtividadeAberta();

    @ManyToOne
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @ManyToOne
    @JoinColumn(name = "problema_id", nullable = false)
    private Problema problema;

    @ManyToOne
    @JoinColumn(name = "estrategia_id", nullable = false)
    private Estrategia estrategia;

    @Column(nullable = false)
    private String llmModelo;

    @Column(nullable = false)
    private String llmUrlApi;

    @JsonIgnore
    @Column(nullable = false)
    private String llmChaveApiCriptografada; // criptografar para assegurar a segurança pedida da chave
                                             // poderiamos so guardar ela e nunca expor no dto

    public boolean aceitaSubmissao() {
        return statusAtual().aceitaSubmissao();
    }

    public boolean concluir() {
        return statusAtual().concluir(this);
    }

    public void mudarEstado(EstadoAtividade novoEstado) {
        this.status = novoEstado;
    }

    private EstadoAtividade statusAtual() {
        if (status == null) {
            status = new AtividadeAberta();
        }
        return status;
    }
}
