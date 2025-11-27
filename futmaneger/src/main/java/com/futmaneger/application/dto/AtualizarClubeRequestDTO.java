package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import java.math.BigDecimal;

public record AtualizarClubeRequestDTO(
        String nome,
        String estado,
        String pais,
        BigDecimal saldo,
        Long tecnicoId
) {
}
