package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.application.dto.RodadaResponseDTO;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class BuscarRodadasPorCampeonatoUseCase {
    private final RodadaRepository rodadaRepository;

    public BuscarRodadasPorCampeonatoUseCase(RodadaRepository rodadaRepository) {
        this.rodadaRepository = rodadaRepository;
    }

    public List<RodadaResponseDTO> executar(Long edicaoId) {
        return rodadaRepository
                .findByEdicaoIdAndFinalizadaFalseOrderByNumero(edicaoId)
                .stream()
                .map(rodada -> new RodadaResponseDTO(
                        rodada.getId(),
                        rodada.getNumero(),
                        rodada.isFinalizada(),
                        rodada.getEdicao().getId()
                ))
                .toList();
    }
}
