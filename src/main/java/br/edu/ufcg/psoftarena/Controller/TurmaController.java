package br.edu.ufcg.psoftarena.Controller;

import br.edu.ufcg.psoftarena.DTO.TurmaPostPutDto;
import br.edu.ufcg.psoftarena.Service.TurmaCrudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/turmas", produces = MediaType.APPLICATION_JSON_VALUE)
public class TurmaController {

    @Autowired
    TurmaCrudService turmaCrudService;

    @PostMapping
    public ResponseEntity<?> addTurma(@RequestBody @Valid TurmaPostPutDto turmaPostPutDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaCrudService.addTurma(turmaPostPutDto));
    }

    @GetMapping
    public ResponseEntity<?> getAllTurmas(@RequestParam(required = false) Long professorId) {
        return ResponseEntity.status(HttpStatus.OK).body(turmaCrudService.getAllTurmas(professorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTurmaById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(turmaCrudService.getTurmaById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTurma(@PathVariable Long id, @RequestBody @Valid TurmaPostPutDto turmaPostPutDto) {
        return ResponseEntity.status(HttpStatus.OK).body(turmaCrudService.updateTurma(id, turmaPostPutDto));
    }

    @PutMapping("/{id}/encerrar")
    public ResponseEntity<?> encerrarTurma(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(turmaCrudService.encerrarTurma(id));
    }
}
