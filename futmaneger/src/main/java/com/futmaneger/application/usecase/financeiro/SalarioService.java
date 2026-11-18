package com.futmaneger.application.usecase.financeiro;

import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.config.FinanceiroProperties;
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

    private final FinanceiroProperties props;
    private final ClubeJpaRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final MovimentacaoFinanceiraService movimentacaoFinanceira;

    public SalarioService(ClubeJpaRepository clubeRepository,
                          JogadorRepository jogadorRepository,
                          MovimentacaoFinanceiraService movimentacaoFinanceira,
                          FinanceiroProperties props) {
        this.props = props;
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
        BigDecimal folhaSalarial = calcularFolha(valorElenco);

        if (folhaSalarial.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        movimentacaoFinanceira.debitarPermitindoNegativo(clube, folhaSalarial,
                CategoriaTransacao.SALARIO, "Folha salarial mensal", null);

        aplicarLimiteDivida(clube, valorElenco);
    }

    public BigDecimal calcularFolha(BigDecimal valorElenco) {
        return valorElenco.multiply(props.getPercentualSalario()).setScale(0, RoundingMode.HALF_UP);
    }

    private void aplicarLimiteDivida(ClubeEntity clube, BigDecimal valorElenco) {
        BigDecimal saldo = clube.getSaldo() == null ? BigDecimal.ZERO : clube.getSaldo();
        if (saldo.compareTo(BigDecimal.ZERO) >= 0) {
            return;
        }

        BigDecimal limiteDivida = valorElenco.multiply(props.getLimiteDividaSobreElenco());
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
