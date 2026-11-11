package com.futmaneger.application.dto;

/**
 * Novo período do campeonato. Enviar os dois meses nulos remove o período e a
 * competição passa a ocupar a temporada inteira.
 */
public record AtualizarPeriodoCampeonatoRequestDTO(
        Integer mesInicio,
        Integer mesFim
) {}
