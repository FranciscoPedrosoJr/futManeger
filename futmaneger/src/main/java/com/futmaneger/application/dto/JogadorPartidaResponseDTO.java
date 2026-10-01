package com.futmaneger.application.dto;

public record JogadorPartidaResponseDTO(
        Long jogadorId,
        String nome,
        double nota,
        int gols,
        int cartoesAmarelos,
        int cartoesVermelhos
) {}
