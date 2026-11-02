package br.edu.ufcg.psoftarena.Controller;

import br.edu.ufcg.psoftarena.DTO.EstudantePostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstudanteResponseDto;
import br.edu.ufcg.psoftarena.Service.EstudanteCrudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/v1/estudantes", produces = MediaType.APPLICATION_JSON_VALUE)
public class EstudanteController {

    @Autowired
    EstudanteCrudService estudanteCrudService;

    @PostMapping("")
    public ResponseEntity<EstudanteResponseDto> criarEstudante(
            @RequestBody @Valid EstudantePostPutDto estudantePostPutDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(estudanteCrudService.criarEstudante(estudantePostPutDto));
    }

    @GetMapping("")
    public ResponseEntity<List<EstudanteResponseDto>> buscarTodosEstudantes() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(estudanteCrudService.buscarTodosEstudantes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstudanteResponseDto> buscarEstudante(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(estudanteCrudService.buscarEstudante(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstudanteResponseDto> editarEstudante(
            @PathVariable Long id,
            @RequestBody @Valid EstudantePostPutDto estudantePostPutDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(estudanteCrudService.editarEstudante(id, estudantePostPutDto));
    }
}