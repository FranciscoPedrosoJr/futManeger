package com.futmaneger.application.dto;

import java.util.List;

public record EscalacaoRequestDTO(

        Long clubeId,
        List<Long> titulares,
        List<Long> reservas,
        String formacao
) {
}
