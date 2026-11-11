package com.futmaneger.application.usecase.financeiro;

import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.config.FinanceiroProperties;
import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceitaInstitucionalService {

    private final JogadorRepository jogadorRepository;
    private final MovimentacaoFinanceiraService movimentacaoFinanceira;
    private final FinanceiroProperties props;

    public ReceitaInstitucionalService(JogadorRepository jogadorRepository,
                                       MovimentacaoFinanceiraService movimentacaoFinanceira,
                                       FinanceiroProperties props) {
        this.jogadorRepository = jogadorRepository;
        this.movimentacaoFinanceira = movimentacaoFinanceira;
        this.props = props;
    }

    @Transactional
    public void creditar(ClubeEntity clube) {
        List<Jogador> elenco = jogadorRepository.findByClube(clube);
        if (elenco.isEmpty()) {
            return;
        }
        BigDecimal valorElenco = valorElenco(elenco);
        BigDecimal cotaTv = calcularCotaTv(valorElenco);
        if (cotaTv.signum() > 0) {
            movimentacaoFinanceira.creditar(clube, cotaTv, CategoriaTransacao.COTA_TV,
                    "Cota de TV do ciclo", null);
        }
        BigDecimal patrocinio = calcularPatrocinio(valorElenco);
        if (patrocinio.signum() > 0) {
            movimentacaoFinanceira.creditar(clube, patrocinio, CategoriaTransacao.PATROCINIO,
                    "Patrocinio do ciclo", null);
        }
    }

    public BigDecimal calcularCotaTv(BigDecimal valorElenco) {
        return valorElenco.multiply(props.getPercentualCotaTv()).setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularPatrocinio(BigDecimal valorElenco) {
        return valorElenco.multiply(props.getPercentualPatrocinio()).setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal valorElenco(List<Jogador> elenco) {
        return elenco.stream()
                .map(Jogador::getValorMercado)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
