package com.futmaneger.application.dto;

import java.math.BigDecimal;

public record AtualizarSaldoResponseDTO(Long id,
                                        String nome,
                                        String estado,
                                        String pais,
                                        BigDecimal saldoAtualizado,
                                        TecnicoDTO tecnico
) {
    public record TecnicoDTO(
            String nome
    ) {}
}
