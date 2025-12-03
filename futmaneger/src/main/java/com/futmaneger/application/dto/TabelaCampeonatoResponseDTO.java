package com.futmaneger.application.dto;

import java.util.List;

public record TabelaCampeonatoResponseDTO(
        Long campeonatoId,
        String nomeCampeonato,
        List<TabelaItemResponseDTO> tabela
) {
}
