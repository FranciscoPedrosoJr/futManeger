package com.futmaneger.application.dto;

public record JogadorResponseDTO(
        Long id,
        String nome,
        String posicao,
        int forca,
        boolean diferenciado,
        boolean identificacaoComClube,
        String nomeClube
) {
}
