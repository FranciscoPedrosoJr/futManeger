package com.futmaneger.application.dto;

import java.time.LocalDate;

/**
 * Resumo de uma edição de campeonato disputada em uma temporada.
 *
 * @param edicaoId            identificador da edição (associação campeonato+temporada)
 * @param campeonatoId        identificador do campeonato (molde reutilizável)
 * @param nome                nome do campeonato
 * @param emAndamento         indica se a disputa desta edição está em andamento
 * @param dataInicioPrevista  início previsto da competição; nulo quando ocupa a temporada inteira
 * @param dataFimPrevista     fim previsto da competição; nulo quando ocupa a temporada inteira
 */
public record TemporadaCampeonatoResponseDTO(
        Long edicaoId,
        Long campeonatoId,
        String nome,
        boolean emAndamento,
        LocalDate dataInicioPrevista,
        LocalDate dataFimPrevista
) {}
