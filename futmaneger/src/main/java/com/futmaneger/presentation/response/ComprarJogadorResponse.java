package com.futmaneger.presentation.response;

import com.futmaneger.infrastructure.persistence.entity.TransacaoFinanceiraEntity;
import java.math.BigDecimal;
import java.util.UUID;

public record ComprarJogadorResponse(UUID idTransacao, Long clubeId, Long jogadorId, String descricao,
                                     BigDecimal valorTransacao, java.time.LocalDateTime data) {
    public ComprarJogadorResponse(TransacaoFinanceiraEntity transacaoFinanceiraEntity){
        this(transacaoFinanceiraEntity.getId(), transacaoFinanceiraEntity.getClubeId(), transacaoFinanceiraEntity.getJogadorId(),
                transacaoFinanceiraEntity.getDescricao(), transacaoFinanceiraEntity.getValor(), transacaoFinanceiraEntity.getData());
    }
}
