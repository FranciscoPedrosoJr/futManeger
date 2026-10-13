package com.futmaneger.application.dto;

/**
 * Resumo de uma edição de campeonato disputada em uma temporada.
 *
 * @param edicaoId      identificador da edição (associação campeonato+temporada)
 * @param campeonatoId  identificador do campeonato (molde reutilizável)
 * @param nome          nome do campeonato
 * @param emAndamento   indica se a disputa desta edição está em andamento
 */
public record TemporadaCampeonatoResponseDTO(
        Long edicaoId,
        Long campeonatoId,
        String nome,
        boolean emAndamento
) {}
