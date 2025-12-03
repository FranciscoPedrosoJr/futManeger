package com.futmaneger.application.dto;

import java.util.List;

public record JogadoresDoClubeResponseDTO(
        Long clubeId,
        String nomeClube,
        List<JogadorDTO> jogadores
) {
    public record JogadorDTO(
            Long id,
            String nome,
            String posicao,
            int forca,
            boolean diferenciado,
            boolean identificacaoComClube
    ) {}
}

