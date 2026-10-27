package com.futmaneger.application.usecase.financeiro;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class PremiacaoService {

    private final MovimentacaoFinanceiraService movimentacaoFinanceira;

    public PremiacaoService(MovimentacaoFinanceiraService movimentacaoFinanceira) {
        this.movimentacaoFinanceira = movimentacaoFinanceira;
    }

    public void premiar(CampeonatoEntity campeonato, ClubeEntity campeao, ClubeEntity vice) {
        BigDecimal premioCampeao = campeonato.getPremiacaoCampeao();
        if (campeao != null && isPositivo(premioCampeao)) {
            movimentacaoFinanceira.creditar(campeao, premioCampeao, CategoriaTransacao.PREMIACAO,
                    "Premiação de campeão: " + campeonato.getNome(), null);
        }

        BigDecimal premioVice = campeonato.getPremiacaoVice();
        if (vice != null && isPositivo(premioVice)) {
            movimentacaoFinanceira.creditar(vice, premioVice, CategoriaTransacao.PREMIACAO,
                    "Premiação de vice-campeão: " + campeonato.getNome(), null);
        }
    }

    private boolean isPositivo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }
}
