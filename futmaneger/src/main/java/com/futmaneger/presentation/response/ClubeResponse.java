package com.futmaneger.presentation.response;

import com.futmaneger.domain.entity.Clube;

public record ClubeResponse(Long id, String nome, String estado, String pais) {
    public ClubeResponse(Clube clube) {
        this(clube.getId(), clube.getNome(), clube.getEstado(), clube.getPais());
    }
}
