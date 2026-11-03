package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.CriterioClassificacao;
import com.futmaneger.infrastructure.persistence.entity.SentidoClassificacao;

public record VinculoClassificacaoResponseDTO(
        Long id,
        Long campeonatoOrigemId,
        String campeonatoOrigemNome,
        Long campeonatoDestinoId,
        String campeonatoDestinoNome,
        int quantidade,
        SentidoClassificacao sentido,
        CriterioClassificacao criterio,
        Integer faseDeEntradaOrdem
) {}
