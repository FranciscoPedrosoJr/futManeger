package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import java.math.BigDecimal;
import java.util.List;

public record AtualizarClubeResponseDTO(Long id, String nome, String estado, String pais, BigDecimal saldo, TecnicoEntity tecnicoId) {
}
