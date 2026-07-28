package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.application.dto.RodadaResponseDTO;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class BuscarRodadasNaoFinalizadasUseCase {
    private final RodadaRepository rodadaRepository;

    public BuscarRodadasNaoFinalizadasUseCase(RodadaRepository rodadaRepository) {
        this.rodadaRepository = rodadaRepository;
    }

    public List<RodadaResponseDTO> executar() {
        return rodadaRepository.findByFinalizadaFalse()
                .stream()
                .map(rodada -> new RodadaResponseDTO(
                        rodada.getId(),
                        rodada.getNumero(),
                        rodada.isFinalizada(),
                        rodada.getCampeonato().getId()
                ))
                .toList();
    }
}
