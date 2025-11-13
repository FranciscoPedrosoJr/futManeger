package com.futmaneger.application.dto;

import java.util.List;

public record GrupoResponseDTO(
        String nome,
        List<String> clubes
) {
}
