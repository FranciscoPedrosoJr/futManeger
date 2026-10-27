package com.futmaneger.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExtratoClubeResponseDTO(
        Long clubeId,
        String clubeNome,
        BigDecimal saldoAtual,
        List<ExtratoItemResponseDTO> transacoes
) {}
