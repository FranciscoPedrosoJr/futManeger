package com.futmaneger.presentation.response;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;

public record ClubeResponse(Long id, String nome, String estado, String pais) {
    public ClubeResponse(ClubeEntity clube) {
        this(clube.getId(), clube.getNome(), clube.getEstado(), clube.getPais());
    }
}
