package br.edu.ufcg.psoftarena.Service;

import br.edu.ufcg.psoftarena.DTO.EstrategiaPostPutDto;
import br.edu.ufcg.psoftarena.DTO.EstrategiaResponseDto;
import br.edu.ufcg.psoftarena.ENUM.TipoEstrategia;
import br.edu.ufcg.psoftarena.Model.Estrategia;
import br.edu.ufcg.psoftarena.Repository.EstrategiaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EstrategiaCrudPadraoService implements EstrategiaCrudService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EstrategiaRepository estrategiaRepository;

    @Override
    public EstrategiaResponseDto criarEstrategia(EstrategiaPostPutDto estrategiaPostPutDto) {
        Estrategia estrategia = modelMapper.map(estrategiaPostPutDto, Estrategia.class);
        return modelMapper.map(estrategiaRepository.save(estrategia), EstrategiaResponseDto.class);
    }

    @Override
    public List<EstrategiaResponseDto> buscarTodasEstrategias(TipoEstrategia tipo) {
        List<Estrategia> estrategias = tipo == null
                ? estrategiaRepository.findAll()
                : estrategiaRepository.findAllByTipo(tipo);
        return estrategias.stream()
                .map(estrategia -> modelMapper.map(estrategia, EstrategiaResponseDto.class))
                .toList();
    }

    @Override
    public EstrategiaResponseDto buscarEstrategia(Long id) {
        return modelMapper.map(getExistingEstrategia(id), EstrategiaResponseDto.class);
    }

    @Override
    public EstrategiaResponseDto editarEstrategia(Long id, EstrategiaPostPutDto estrategiaPostPutDto) {
        Estrategia estrategia = getExistingEstrategia(id);
        estrategia.setNome(estrategiaPostPutDto.getNome());
        estrategia.setTipo(estrategiaPostPutDto.getTipo());
        estrategia.setDescricao(estrategiaPostPutDto.getDescricao());
        estrategia.setPrompt(estrategiaPostPutDto.getPrompt());
        return modelMapper.map(estrategiaRepository.save(estrategia), EstrategiaResponseDto.class);
    }

    private Estrategia getExistingEstrategia(Long id) {
        return estrategiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estratégia não encontrada"));
    }
}
