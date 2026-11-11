package com.futmaneger.application.dto;

public record AtualizarPeriodoCampeonatoResponseDTO(
        Long id,
        String nome,
        Integer mesInicio,
        Integer mesFim,
        int edicoesAtualizadas,
        int edicoesMantidas
) {}
