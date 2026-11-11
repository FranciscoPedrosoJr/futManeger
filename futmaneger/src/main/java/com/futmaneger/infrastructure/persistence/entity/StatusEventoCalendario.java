package com.futmaneger.infrastructure.persistence.entity;

import java.util.List;

public enum StatusEventoCalendario {
    PENDENTE,
    EM_PROCESSAMENTO,
    CONCLUIDO,
    CANCELADO,
    ERRO;

    /**
     * Situações em que o evento ainda precisa ser processado. Eventos com erro e
     * em processamento continuam elegíveis para permitir a retomada do avanço.
     */
    public static List<StatusEventoCalendario> abertos() {
        return List.of(PENDENTE, EM_PROCESSAMENTO, ERRO);
    }
}
