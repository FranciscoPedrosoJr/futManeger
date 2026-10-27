package com.futmaneger.application.dto;

public record CotaEstadualResponseDTO(
        Long id,
        Long campeonatoId,
        String campeonatoNome,
        String estado,
        int quantidade,
        Integer faseDeEntradaOrdem
) {}
