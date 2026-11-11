package com.futmaneger.application.dto;

import java.time.LocalDate;

public record ResultadoPartidaResumoDTO(
        Long eventoId,
        LocalDate data,
        String campeonato,
        String mandante,
        int golsMandante,
        String visitante,
        int golsVisitante
) {}
