package com.futmaneger.application.dto;

public record TabelaItemResponseDTO(
        int posicao,
        Long clubeId,
        String nomeClube,
        int pontos,
        int vitorias,
        int empates,
        int derrotas,
        int golsMarcados,
        int golsSofridos,
        int saldoGols
) {}
