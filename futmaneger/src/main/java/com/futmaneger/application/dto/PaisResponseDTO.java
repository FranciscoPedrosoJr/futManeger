package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.Continente;

public record PaisResponseDTO(
        Long id,
        String nome,
        Continente continente
) {}
