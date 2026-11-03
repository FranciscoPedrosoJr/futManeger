package com.futmaneger.application.dto;

import java.util.List;

public record GerarRodadasResponseDTO(
        Long edicaoId,
        int totalRodadas,
        int totalPartidas,

        List<GrupoResponseDTO> grupos

) {}
