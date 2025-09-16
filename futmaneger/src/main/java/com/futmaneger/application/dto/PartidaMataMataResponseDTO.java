package com.futmaneger.application.dto;

public record PartidaMataMataResponseDTO(
        String campeonato,
        int fase,
        String clubeMandante,
        String clubeVisitante,
        boolean finalizada
) {
}
