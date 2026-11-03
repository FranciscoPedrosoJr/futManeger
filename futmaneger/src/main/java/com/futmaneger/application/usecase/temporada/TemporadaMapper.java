package com.futmaneger.application.usecase.temporada;

import com.futmaneger.application.dto.TemporadaCampeonatoResponseDTO;
import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import java.util.List;

final class TemporadaMapper {

    private TemporadaMapper() {
    }

    static TemporadaResponseDTO toResponse(TemporadaEntity temporada) {
        List<TemporadaCampeonatoResponseDTO> edicoes = temporada.getEdicoes().stream()
                .map(TemporadaMapper::toEdicaoResponse)
                .toList();

        return new TemporadaResponseDTO(
                temporada.getId(),
                temporada.getNome(),
                temporada.getAno(),
                temporada.getEstado().name(),
                temporada.getDataInicio(),
                temporada.getDataEncerramento(),
                edicoes
        );
    }

    private static TemporadaCampeonatoResponseDTO toEdicaoResponse(EdicaoCampeonatoEntity edicao) {
        return new TemporadaCampeonatoResponseDTO(
                edicao.getId(),
                edicao.getCampeonato().getId(),
                edicao.getCampeonato().getNome(),
                edicao.getEmAndamento()
        );
    }
}
