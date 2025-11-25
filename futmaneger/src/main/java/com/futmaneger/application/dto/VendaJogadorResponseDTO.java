package com.futmaneger.application.dto;

import java.math.BigDecimal;

public record VendaJogadorResponseDTO(
        Long jogadorId,
        String nomeJogador,
        String clubeAnterior,
        String clubeComprador,
        BigDecimal valorTransacao,
        String motivoEscolhaComprador
) {}

