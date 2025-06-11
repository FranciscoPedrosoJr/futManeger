package com.futmaneger.presentation.response;

import com.futmaneger.domain.entity.Clube;

public record ClubeResponse(Long id, String nome, String cidade) {
    public ClubeResponse(Clube clube) {
        this(clube.getId(), clube.getNome(), clube.getCidade());
    }
}
