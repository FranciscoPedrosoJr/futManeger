package com.futmaneger.application.dto;

import java.math.BigDecimal;

public record TecnicoClubeResponseDTO(
        Long clubeId,
        String clubeNome,
        String clubeEstado,
        String clubePais,
        BigDecimal clubeSaldo
) {
}

