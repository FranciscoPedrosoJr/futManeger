package com.futmaneger.application.dto;

public record SimulacaoResponseDTO(
        String nomeMandante,
        int golsMandante,
        String nomeVisitante,
        int golsVisitante,
        String resultado
) {}
