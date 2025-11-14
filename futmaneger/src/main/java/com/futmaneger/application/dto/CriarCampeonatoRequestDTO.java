package com.futmaneger.application.dto;

import java.util.List;

public record CriarCampeonatoRequestDTO(
        String nome,
        String pais,
        String estado,
        List<Long>clubes
) {}
