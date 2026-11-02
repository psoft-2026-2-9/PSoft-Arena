package br.edu.ufcg.psoftarena.Controller;

import br.edu.ufcg.psoftarena.DTO.EstrategiaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstrategiaResponseDto;
import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import br.edu.ufcg.psoftarena.Service.EstrategiaCrudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/estrategias", produces = MediaType.APPLICATION_JSON_VALUE)
public class EstrategiaController {

    @Autowired
    EstrategiaCrudService estrategiaCrudService;

    @PostMapping
    public ResponseEntity<EstrategiaResponseDto> criarEstrategia(
            @RequestBody @Valid EstrategiaPostPutDto estrategiaPostPutDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(estrategiaCrudService.criarEstrategia(estrategiaPostPutDto));
    }

    @GetMapping
    public ResponseEntity<List<EstrategiaResponseDto>> buscarTodasEstrategias(
            @RequestParam(required = false) TipoEstrategia tipo) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(estrategiaCrudService.buscarTodasEstrategias(tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstrategiaResponseDto> buscarEstrategia(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(estrategiaCrudService.buscarEstrategia(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstrategiaResponseDto> editarEstrategia(
            @PathVariable Long id,
            @RequestBody @Valid EstrategiaPostPutDto estrategiaPostPutDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(estrategiaCrudService.editarEstrategia(id, estrategiaPostPutDto));
    }
}
