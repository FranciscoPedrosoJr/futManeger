package com.futmaneger.application.dto;

import java.math.BigDecimal;

public record AtualizarSaldoRequestDTO(
        Long clubeId,
        BigDecimal valor
) {}
