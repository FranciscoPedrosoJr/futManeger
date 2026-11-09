package com.futmaneger.application.dto;

import java.time.LocalDate;
import java.util.List;

public record TemporadaResponseDTO(
        Long id,
        String nome,
        Integer ano,
        String estado,
        LocalDate dataInicio,
        LocalDate dataAtual,
        LocalDate dataEncerramento,
        boolean calendarioGerado,
        List<TemporadaCampeonatoResponseDTO> campeonatos
) {}
