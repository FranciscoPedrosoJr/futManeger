package com.futmaneger.application.dto;

public enum SituacaoAvanco {
    /** O avanço parou em uma partida do clube do usuário, que precisa ser disputada. */
    AGUARDANDO_USUARIO,
    /** Não há mais acontecimentos pendentes; a temporada pode ser avaliada para encerramento. */
    SEM_PENDENCIAS,
    /** O avanço atingiu o limite de trabalho da chamada e pode ser retomado. */
    LIMITE_ATINGIDO,
    /** Um acontecimento falhou; ele permanece identificado e o avanço pode ser retomado. */
    ERRO
}
