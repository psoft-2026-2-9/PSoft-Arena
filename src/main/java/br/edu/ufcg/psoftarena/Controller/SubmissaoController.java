package br.edu.ufcg.psoftarena.Controller;

import br.edu.ufcg.psoftarena.DTO.SubmissaoPostDto;
import br.edu.ufcg.psoftarena.DTO.SubmissaoResponseDto;
import java.util.List;
import br.edu.ufcg.psoftarena.Service.SubmissaoCrudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/atividades/{atividadeId}/submissoes", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubmissaoController {

    @Autowired
    SubmissaoCrudService submissaoCrudService;

    @GetMapping
    public ResponseEntity<List<SubmissaoResponseDto>> listarSubmissoes(
            @PathVariable Long atividadeId, @RequestParam Long estudanteId) {
        return ResponseEntity.ok(submissaoCrudService.listarSubmissoes(atividadeId, estudanteId));
    }

    @GetMapping("/{submissaoId}")
    public ResponseEntity<SubmissaoResponseDto> consultarSubmissao(
            @PathVariable Long atividadeId, @PathVariable Long submissaoId,
            @RequestParam Long estudanteId) {
        return ResponseEntity.ok(submissaoCrudService.consultarSubmissao(atividadeId, submissaoId, estudanteId));
    }

    @PostMapping
    public ResponseEntity<SubmissaoResponseDto> submeterSolucao(@PathVariable Long atividadeId,
                                             @RequestBody @Valid SubmissaoPostDto submissaoPostDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissaoCrudService.submeterSolucao(atividadeId, submissaoPostDto));
    }
}
