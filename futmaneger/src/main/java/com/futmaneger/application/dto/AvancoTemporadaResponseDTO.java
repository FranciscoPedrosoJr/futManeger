package com.futmaneger.application.dto;

import java.time.LocalDate;
import java.util.List;

public record AvancoTemporadaResponseDTO(
        Long temporadaId,
        LocalDate dataAtual,
        SituacaoAvanco situacao,
        int partidasSimuladas,
        List<ResultadoPartidaResumoDTO> resultados,
        EventoCalendarioResponseDTO proximoEvento,
        boolean podeFinalizar,
        String mensagem
) {}
