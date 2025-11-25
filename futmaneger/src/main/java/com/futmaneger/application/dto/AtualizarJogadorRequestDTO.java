package com.futmaneger.application.dto;

public record AtualizarJogadorRequestDTO(
        String nome,
        String posicao,
        Integer forca,
        Boolean diferenciado,
        Boolean identificacaoComClube,
        Long clubeId
) {}

