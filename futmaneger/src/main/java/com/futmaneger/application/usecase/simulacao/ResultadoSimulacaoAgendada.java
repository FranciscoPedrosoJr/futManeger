package com.futmaneger.application.usecase.simulacao;

import com.futmaneger.application.dto.SimulacaoResponseDTO;

/** Resultado interno da simulação usada pelo calendário. */
public record ResultadoSimulacaoAgendada(
        SimulacaoResponseDTO simulacao,
        boolean rodadaConcluida
) {
}
