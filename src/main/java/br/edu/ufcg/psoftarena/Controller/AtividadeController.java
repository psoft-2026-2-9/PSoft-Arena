package br.edu.ufcg.psoftarena.Controller;

import br.edu.ufcg.psoftarena.DTO.AtividadePostDto;
import br.edu.ufcg.psoftarena.DTO.AtividadeResponseDto;
import br.edu.ufcg.psoftarena.Service.AtividadeCrudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import br.edu.ufcg.psoftarena.DTO.AtividadeConsultaResponseDto;
import java.util.List;

@RestController
@RequestMapping(value = "/atividades", produces = MediaType.APPLICATION_JSON_VALUE)
public class AtividadeController {

    @Autowired
    AtividadeCrudService atividadeService;

    @PostMapping
    public ResponseEntity<AtividadeResponseDto> criarAtividade(@RequestBody @Valid AtividadePostDto atividadePostDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atividadeService.criarAtividade(atividadePostDto));
    }

    @PutMapping("/{id}/concluir")
    public ResponseEntity<AtividadeResponseDto> concluirAtividade(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(atividadeService.concluirAtividade(id));
    }

    @GetMapping("/estudante/{estudanteId}")
    public ResponseEntity<List<AtividadeConsultaResponseDto>> listarAtividadesDoEstudante(
            @PathVariable Long estudanteId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(atividadeService.listarAtividadesDoEstudante(estudanteId));
    }

    @GetMapping("/{atividadeId}/estudante/{estudanteId}")
    public ResponseEntity<AtividadeConsultaResponseDto> consultarAtividadeDoEstudante(
            @PathVariable Long atividadeId,
            @PathVariable Long estudanteId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(atividadeService.consultarAtividadeDoEstudante(atividadeId, estudanteId));
    }
}
