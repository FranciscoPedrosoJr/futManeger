package com.futmaneger.application.dto;

import java.time.LocalDate;

public record EventoCalendarioResponseDTO(
        Long id,
        String tipo,
        LocalDate data,
        String status,
        boolean obrigatorio,
        Long edicaoId,
        Long campeonatoId,
        String campeonatoNome,
        Integer rodada,
        Long partidaId,
        boolean partidaMataMata,
        Long mandanteId,
        String mandante,
        Long visitanteId,
        String visitante,
        Integer golsMandante,
        Integer golsVisitante,
        boolean envolveClubeDoUsuario,
        String mensagemErro
) {}
