package com.futmaneger.application.dto;

import java.math.BigDecimal;

public record AtualizarTecnicoRequestDTO(
        String nome,
        String email,
        String senha
) {}
