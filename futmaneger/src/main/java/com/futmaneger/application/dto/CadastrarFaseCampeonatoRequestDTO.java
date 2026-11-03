package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.FormatoFase;

public record CadastrarFaseCampeonatoRequestDTO(
        Long campeonatoId,
        int ordem,
        String nome,
        FormatoFase formato
) {}
