package com.futmaneger.application.usecase.financeiro;

import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalarioService {

    private static final BigDecimal PERCENTUAL_SALARIO = new BigDecimal("0.02");
    private static final BigDecimal LIMITE_DIVIDA_SOBRE_ELENCO = new BigDecimal("0.35");

    private final ClubeJpaRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final MovimentacaoFinanceiraService movimentacaoFinanceira;

    public SalarioService(ClubeJpaRepository clubeRepository,
                          JogadorRepository jogadorRepository,
                          MovimentacaoFinanceiraService movimentacaoFinanceira) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.movimentacaoFinanceira = movimentacaoFinanceira;
    }

    @Transactional
    public void cobrarSalarios(ClubeEntity clube) {
        List<Jogador> elenco = jogadorRepository.findByClube(clube);
        if (elenco.isEmpty()) {
            return;
        }

        BigDecimal valorElenco = somaValorElenco(elenco);
        BigDecimal folhaSalarial = valorElenco.multiply(PERCENTUAL_SALARIO)
                .setScale(0, RoundingMode.HALF_UP);

        if (folhaSalarial.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        movimentacaoFinanceira.debitarPermitindoNegativo(clube, folhaSalarial,
                CategoriaTransacao.SALARIO, "Folha salarial mensal", null);

        aplicarLimiteDivida(clube, valorElenco);
    }

    private void aplicarLimiteDivida(ClubeEntity clube, BigDecimal valorElenco) {
        BigDecimal saldo = clube.getSaldo() == null ? BigDecimal.ZERO : clube.getSaldo();
        if (saldo.compareTo(BigDecimal.ZERO) >= 0) {
            return;
        }

        BigDecimal limiteDivida = valorElenco.multiply(LIMITE_DIVIDA_SOBRE_ELENCO);
        BigDecimal dividaAtual = saldo.abs();

        if (dividaAtual.compareTo(limiteDivida) > 0 && clube.getTecnico() != null) {
            clube.setTecnico(null);
            clubeRepository.save(clube);
        }
    }

    private BigDecimal somaValorElenco(List<Jogador> elenco) {
        return elenco.stream()
                .map(Jogador::getValorMercado)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
