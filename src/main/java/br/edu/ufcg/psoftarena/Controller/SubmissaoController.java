package br.edu.ufcg.psoftarena.Controller;

import br.edu.ufcg.psoftarena.DTO.SubmissaoPostDto;
import br.edu.ufcg.psoftarena.Service.SubmissaoCrudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/atividades/{atividadeId}/submissoes", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubmissaoController {

    @Autowired
    SubmissaoCrudService submissaoCrudService;

    @PostMapping
    public ResponseEntity<?> submeterSolucao(@PathVariable Long atividadeId,
                                             @RequestBody @Valid SubmissaoPostDto submissaoPostDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissaoCrudService.submeterSolucao(atividadeId, submissaoPostDto));
    }
}
