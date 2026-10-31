package br.edu.ufcg.psoftarena.Controller;

import br.edu.ufcg.psoftarena.DTO.ProfessorPostPutDto;
import br.edu.ufcg.psoftarena.Service.ProfessorCrudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/professores", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfessorController {

    @Autowired
    ProfessorCrudService professorCrudService;

    @PostMapping
    public ResponseEntity<?> addProfessor(@RequestBody @Valid ProfessorPostPutDto professorPostPutDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professorCrudService.addProfessor(professorPostPutDto));
    }

    @GetMapping
    public ResponseEntity<?> getAllProfessors() {
        return ResponseEntity.status(HttpStatus.OK).body(professorCrudService.getAllProfessors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfessorById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(professorCrudService.getProfessorById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfessor(@PathVariable Long id, @RequestBody @Valid ProfessorPostPutDto professorPostPutDto) {
        return ResponseEntity.status(HttpStatus.OK).body(professorCrudService.updateProfessor(id, professorPostPutDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfessor(@PathVariable Long id) {
        professorCrudService.deleteProfessor(id);
        return ResponseEntity.noContent().build();
    }
}
