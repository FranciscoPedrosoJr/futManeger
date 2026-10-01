package com.futmaneger.application.usecase.temporada;

import com.futmaneger.application.dto.TemporadaCampeonatoResponseDTO;
import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import java.util.List;

final class TemporadaMapper {

    private TemporadaMapper() {
    }

    static TemporadaResponseDTO toResponse(TemporadaEntity temporada) {
        List<TemporadaCampeonatoResponseDTO> campeonatos = temporada.getCampeonatos().stream()
                .map(TemporadaMapper::toCampeonatoResponse)
                .toList();

        return new TemporadaResponseDTO(
                temporada.getId(),
                temporada.getNome(),
                temporada.getAno(),
                temporada.getEstado().name(),
                temporada.getDataInicio(),
                temporada.getDataEncerramento(),
                campeonatos
        );
    }

    private static TemporadaCampeonatoResponseDTO toCampeonatoResponse(CampeonatoEntity campeonato) {
        return new TemporadaCampeonatoResponseDTO(
                campeonato.getId(),
                campeonato.getNome(),
                campeonato.getEmAndamento()
        );
    }
}
