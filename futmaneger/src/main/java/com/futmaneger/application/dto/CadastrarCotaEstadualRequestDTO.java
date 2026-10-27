package com.futmaneger.application.dto;

public record CadastrarCotaEstadualRequestDTO(
        Long campeonatoId,
        String estado,
        int quantidade,
        Integer faseDeEntradaOrdem
) {}
