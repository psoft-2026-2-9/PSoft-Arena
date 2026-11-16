package br.edu.ufcg.psoftarena.Model;

import br.edu.ufcg.psoftarena.ENUM.StatusAtividade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAtividade status;

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
}
