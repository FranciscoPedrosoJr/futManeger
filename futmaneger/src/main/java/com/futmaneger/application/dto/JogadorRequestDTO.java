package com.futmaneger.application.dto;

import java.util.List;

public record JogadorRequestDTO(
        String nome,
        String posicao,
        int forca,
        boolean diferenciado,
        boolean identificacaoComClube,
        Long clubeId
) {}

