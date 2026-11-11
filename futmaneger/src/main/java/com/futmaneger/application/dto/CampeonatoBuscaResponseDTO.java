package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;

public record CampeonatoBuscaResponseDTO (
        Long id,
        String nome,
        CampeonatoEntity.TipoCampeonato tipo,
        int quantidadeDeClubes,
        String pais,
        Integer mesInicio,
        Integer mesFim
) {
}
