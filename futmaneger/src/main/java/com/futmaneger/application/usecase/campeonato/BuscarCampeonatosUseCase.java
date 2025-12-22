package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.CampeonatoBuscaResponseDTO;
import com.futmaneger.application.dto.CampeonatoResponseDTO;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BuscarCampeonatosUseCase {

    private final CampeonatoRepository campeonatoRepository;

    public BuscarCampeonatosUseCase(CampeonatoRepository campeonatoRepository) {
        this.campeonatoRepository = campeonatoRepository;
    }

    public List<CampeonatoBuscaResponseDTO> executar() {
        return campeonatoRepository.findAll()
                .stream()
                .map(campeonato -> new CampeonatoBuscaResponseDTO(
                        campeonato.getId(),
                        campeonato.getNome(),
                        campeonato.getEmAndamento(),
                        campeonato.getTipo(),
                        campeonato.getQuantidadeClubes(),
                        campeonato.getPais()
                ))
                .toList();
    }
}

