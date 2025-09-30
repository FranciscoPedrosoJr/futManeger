package com.futmaneger.application.dto;

public record PartidaMataMataResponseDTO(
        String campeonato,
        com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity.FaseMataMata fase,
        String clubeMandante,
        String clubeVisitante,
        boolean finalizada
) {
}
