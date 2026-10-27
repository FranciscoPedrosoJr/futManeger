package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExtratoItemResponseDTO(
        UUID id,
        BigDecimal valor,
        CategoriaTransacao categoria,
        String descricao,
        Long jogadorId,
        LocalDateTime data
) {}
