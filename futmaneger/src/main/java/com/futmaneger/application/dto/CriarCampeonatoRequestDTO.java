package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.AbrangenciaCampeonato;
import java.util.List;

public record CriarCampeonatoRequestDTO(
        String nome,
        String pais,
        String estado,
        AbrangenciaCampeonato abrangencia,
        List<Long> clubes
) {}
