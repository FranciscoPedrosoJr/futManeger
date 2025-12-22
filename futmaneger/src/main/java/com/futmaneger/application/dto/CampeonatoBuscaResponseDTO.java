package com.futmaneger.application.dto;

public record CampeonatoBuscaResponseDTO (
        Long id,
        String nome,
        Boolean emAndamento,
        Enum tipo,
        int quantidadeDeClubes,
        String pais
) {
}
