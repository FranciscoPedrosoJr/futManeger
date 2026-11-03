package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.FormatoFase;

public record FaseCampeonatoResponseDTO(
        Long id,
        Long campeonatoId,
        int ordem,
        String nome,
        FormatoFase formato
) {}
