package com.futmaneger.application.dto;

import java.math.BigDecimal;

public record AtualizarJogadorRequestDTO(
        String nome,
        String posicao,
        Integer forca,
        Boolean diferenciado,
        Boolean identificacaoComClube,
        BigDecimal valorMercado,
        Long clubeId
) {}

