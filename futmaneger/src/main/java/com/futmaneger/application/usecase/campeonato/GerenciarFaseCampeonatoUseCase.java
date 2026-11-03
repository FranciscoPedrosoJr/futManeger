package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.CadastrarFaseCampeonatoRequestDTO;
import com.futmaneger.application.dto.FaseCampeonatoResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.FaseCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.FaseCampeonatoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GerenciarFaseCampeonatoUseCase {

    private final FaseCampeonatoRepository faseRepository;
    private final CampeonatoRepository campeonatoRepository;

    public GerenciarFaseCampeonatoUseCase(FaseCampeonatoRepository faseRepository,
                                          CampeonatoRepository campeonatoRepository) {
        this.faseRepository = faseRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    @Transactional
    public FaseCampeonatoResponseDTO cadastrar(CadastrarFaseCampeonatoRequestDTO request) {
        if (request.campeonatoId() == null) {
            throw new DadosInvalidosException("O campeonato é obrigatório");
        }
        if (request.ordem() <= 0) {
            throw new DadosInvalidosException("A ordem da fase deve ser maior que zero");
        }
        if (request.nome() == null || request.nome().isBlank()) {
            throw new DadosInvalidosException("O nome da fase é obrigatório");
        }
        if (request.formato() == null) {
            throw new DadosInvalidosException("O formato da fase é obrigatório");
        }

        CampeonatoEntity campeonato = campeonatoRepository.findById(request.campeonatoId())
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));

        if (faseRepository.existsByCampeonatoAndOrdem(campeonato, request.ordem())) {
            throw new DadosInvalidosException(
                    "Já existe uma fase com a ordem " + request.ordem() + " neste campeonato");
        }

        FaseCampeonatoEntity fase = faseRepository.save(
                new FaseCampeonatoEntity(campeonato, request.ordem(), request.nome(), request.formato()));

        return toResponse(fase);
    }

    public List<FaseCampeonatoResponseDTO> listarPorCampeonato(Long campeonatoId) {
        CampeonatoEntity campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));
        return faseRepository.findByCampeonatoOrderByOrdem(campeonato).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void remover(Long id) {
        if (!faseRepository.existsById(id)) {
            throw new NaoEncontradoException("Fase não encontrada");
        }
        faseRepository.deleteById(id);
    }

    private FaseCampeonatoResponseDTO toResponse(FaseCampeonatoEntity fase) {
        return new FaseCampeonatoResponseDTO(
                fase.getId(),
                fase.getCampeonato().getId(),
                fase.getOrdem(),
                fase.getNome(),
                fase.getFormato()
        );
    }
}
