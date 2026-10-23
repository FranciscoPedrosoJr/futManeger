package com.futmaneger.application.dto;

import java.util.List;

/**
 * Request para adicionar um campeonato a uma temporada informando explicitamente
 * os clubes que irão participar da edição.
 */
public record AdicionarCampeonatoComClubesRequestDTO(
        List<Long> clubesIds
) {
}
