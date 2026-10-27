package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.CriterioClassificacao;
import com.futmaneger.infrastructure.persistence.entity.SentidoClassificacao;

public record CadastrarVinculoClassificacaoRequestDTO(
        Long campeonatoOrigemId,
        Long campeonatoDestinoId,
        int quantidade,
        SentidoClassificacao sentido,
        CriterioClassificacao criterio
) {}
