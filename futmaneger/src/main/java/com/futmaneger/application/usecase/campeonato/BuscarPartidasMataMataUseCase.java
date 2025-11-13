package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.PartidaMataMataResponseDTO;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BuscarPartidasMataMataUseCase {
    private final PartidaMataMataRepository partidaMataMataRepository;

    public BuscarPartidasMataMataUseCase(PartidaMataMataRepository partidaMataMataRepository) {
        this.partidaMataMataRepository = partidaMataMataRepository;
    }

    public List<PartidaMataMataResponseDTO> buscarPorCampeonato(Long campeonatoId) {
        return partidaMataMataRepository.findByCampeonatoId(campeonatoId).stream()
                .map(partida -> new PartidaMataMataResponseDTO(
                        partida.getCampeonato().getNome(),
                        partida.getFase(),
                        partida.getClubeA().getNome(),
                        partida.getClubeB().getNome(),
                        partida.isFinalizada()
                ))
                .toList();
    }
}
