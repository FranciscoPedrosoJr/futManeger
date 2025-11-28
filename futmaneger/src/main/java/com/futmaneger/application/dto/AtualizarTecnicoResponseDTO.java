package com.futmaneger.application.dto;

public record AtualizarTecnicoResponseDTO(
        Long id,
        String nome,
        String email,
        String senha
) {}
