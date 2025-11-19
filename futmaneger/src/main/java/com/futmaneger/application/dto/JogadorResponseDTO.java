package com.futmaneger.application.dto;

import java.math.BigDecimal;

public record JogadorResponseDTO(
        Long id,
        String nome,
        String posicao,
        int forca,
        boolean diferenciado,
        boolean identificacaoComClube,
        String nomeClube,
        BigDecimal valorMercado
) {
}
