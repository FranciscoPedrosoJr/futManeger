package com.futmaneger.infrastructure.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "futmaneger.financeiro")
public class FinanceiroProperties {

    private BigDecimal percentualSalario = new BigDecimal("0.02");
    private BigDecimal percentualCotaTv = new BigDecimal("0.011");
    private BigDecimal percentualPatrocinio = new BigDecimal("0.008");
    private BigDecimal limiteDividaSobreElenco = new BigDecimal("0.35");
    private BigDecimal precoIngressoPadrao = new BigDecimal("50");
    private int rodadasPorCiclo = 6;

    public BigDecimal getPercentualSalario() { return percentualSalario; }
    public void setPercentualSalario(BigDecimal v) { this.percentualSalario = v; }

    public BigDecimal getPercentualCotaTv() { return percentualCotaTv; }
    public void setPercentualCotaTv(BigDecimal v) { this.percentualCotaTv = v; }

    public BigDecimal getPercentualPatrocinio() { return percentualPatrocinio; }
    public void setPercentualPatrocinio(BigDecimal v) { this.percentualPatrocinio = v; }

    public BigDecimal getLimiteDividaSobreElenco() { return limiteDividaSobreElenco; }
    public void setLimiteDividaSobreElenco(BigDecimal v) { this.limiteDividaSobreElenco = v; }

    public BigDecimal getPrecoIngressoPadrao() { return precoIngressoPadrao; }
    public void setPrecoIngressoPadrao(BigDecimal v) { this.precoIngressoPadrao = v; }

    public int getRodadasPorCiclo() { return rodadasPorCiclo; }
    public void setRodadasPorCiclo(int v) { this.rodadasPorCiclo = v; }
}
