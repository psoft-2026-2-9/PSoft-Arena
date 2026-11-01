package br.edu.ufcg.psoftarena.Controller;

import br.edu.ufcg.psoftarena.DTO.ProblemaPostPutDto;
import br.edu.ufcg.psoftarena.Service.ProblemaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/problemas", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProblemaController {

    @Autowired
    ProblemaService problemaService;

    @PostMapping
    public ResponseEntity<?> addProblema(@RequestBody @Valid ProblemaPostPutDto problemaPostPutDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(problemaService.addProblema(problemaPostPutDto));
    }

    @GetMapping
    public ResponseEntity<?> getAllProblemas() {
        return ResponseEntity.status(HttpStatus.OK).body(problemaService.getAllProblemas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProblemaById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(problemaService.getProblemaById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProblema(@PathVariable Long id, @RequestBody @Valid ProblemaPostPutDto problemaPostPutDto) {
        return ResponseEntity.status(HttpStatus.OK).body(problemaService.updateProblema(id, problemaPostPutDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProblema(@PathVariable Long id) {
        problemaService.deleteProblema(id);
        return ResponseEntity.noContent().build();
    }
}
