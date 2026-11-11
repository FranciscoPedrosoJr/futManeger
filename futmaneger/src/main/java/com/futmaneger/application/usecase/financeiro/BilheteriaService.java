package com.futmaneger.application.usecase.financeiro;

import com.futmaneger.infrastructure.config.FinanceiroProperties;
import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class BilheteriaService {

    private static final int PUBLICO_MAXIMO = 100_000;

    private final ClubeJpaRepository clubeRepository;
    private final MovimentacaoFinanceiraService movimentacaoFinanceira;
    private final FinanceiroProperties props;

    public BilheteriaService(ClubeJpaRepository clubeRepository,
                             MovimentacaoFinanceiraService movimentacaoFinanceira,
                             FinanceiroProperties props) {
        this.clubeRepository = clubeRepository;
        this.movimentacaoFinanceira = movimentacaoFinanceira;
        this.props = props;
    }

    public void registrarBilheteria(ClubeEntity mandante, ClubeEntity visitante) {
        BigDecimal preco = precoEfetivo(mandante);
        if (preco.signum() <= 0) {
            return;
        }

        int publico = estimarPublico(forca(mandante), forca(visitante));
        if (publico <= 0) {
            return;
        }

        BigDecimal receita = preco.multiply(BigDecimal.valueOf(publico));
        movimentacaoFinanceira.creditar(mandante, receita, CategoriaTransacao.BILHETERIA,
                "Bilheteria: " + mandante.getNome() + " x " + visitante.getNome() + " ("
                        + publico + " pagantes)", null);
    }

    /** Clubes sem tecnico e sem preco definido usam o preco padrao; clubes de usuario nao. */
    public BigDecimal precoEfetivo(ClubeEntity clube) {
        BigDecimal preco = clube.getPrecoIngresso();
        if (preco != null && preco.signum() > 0) {
            return preco;
        }
        if (clube.getTecnico() == null) {
            return props.getPrecoIngressoPadrao();
        }
        return BigDecimal.ZERO;
    }

    public boolean usaPrecoPadrao(ClubeEntity clube) {
        BigDecimal preco = clube.getPrecoIngresso();
        return (preco == null || preco.signum() <= 0) && clube.getTecnico() == null;
    }

    public BigDecimal estimarReceita(ClubeEntity mandante, ClubeEntity visitante) {
        return estimarReceita(mandante, forca(mandante), forca(visitante));
    }

    public BigDecimal estimarReceita(ClubeEntity mandante, double forcaMandante, double forcaVisitante) {
        BigDecimal preco = precoEfetivo(mandante);
        int publico = estimarPublico(forcaMandante, forcaVisitante);
        if (preco.signum() <= 0 || publico <= 0) {
            return BigDecimal.ZERO;
        }
        return preco.multiply(BigDecimal.valueOf(publico));
    }

    public int estimarPublico(double forcaMandante, double forcaVisitante) {
        double media = (forcaMandante + forcaVisitante) / 2.0;
        double diferenca = Math.abs(forcaMandante - forcaVisitante);
        double fracaoBase = clamp(media / 100.0, 0.0, 1.0);
        double fatorEquilibrio = 1.0 - clamp(diferenca / 100.0, 0.0, 1.0) * 0.4;
        double publico = PUBLICO_MAXIMO * fracaoBase * fatorEquilibrio;
        return (int) Math.round(publico);
    }

    public double forca(ClubeEntity clube) {
        Double media = clubeRepository.calcularMediaForca(clube.getId());
        return media == null ? 0.0 : media;
    }

    private double clamp(double valor, double min, double max) {
        return Math.max(min, Math.min(max, valor));
    }
}
