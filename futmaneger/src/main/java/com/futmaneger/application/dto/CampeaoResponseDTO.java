package com.futmaneger.application.dto;

public record CampeaoResponseDTO(
        Long campeonatoId,
        String nomeCampeonato,
        Long clubeCampeaoId,
        String nomeClubeCampeao,
        int pontos,
        int vitorias,
        int empates,
        int derrotas,
        int saldoGols
) {}
