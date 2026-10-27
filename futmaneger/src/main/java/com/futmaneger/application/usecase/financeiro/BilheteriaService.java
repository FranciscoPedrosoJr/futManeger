package com.futmaneger.application.usecase.financeiro;

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

    public BilheteriaService(ClubeJpaRepository clubeRepository,
                             MovimentacaoFinanceiraService movimentacaoFinanceira) {
        this.clubeRepository = clubeRepository;
        this.movimentacaoFinanceira = movimentacaoFinanceira;
    }

    public void registrarBilheteria(ClubeEntity mandante, ClubeEntity visitante) {
        BigDecimal preco = mandante.getPrecoIngresso();
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
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

    private int estimarPublico(double forcaMandante, double forcaVisitante) {
        double media = (forcaMandante + forcaVisitante) / 2.0;
        double diferenca = Math.abs(forcaMandante - forcaVisitante);

        double fracaoBase = clamp(media / 100.0, 0.0, 1.0);
        double fatorEquilibrio = 1.0 - clamp(diferenca / 100.0, 0.0, 1.0) * 0.4;

        double publico = PUBLICO_MAXIMO * fracaoBase * fatorEquilibrio;
        return (int) Math.round(publico);
    }

    private double forca(ClubeEntity clube) {
        Double media = clubeRepository.calcularMediaForca(clube.getId());
        return media == null ? 0.0 : media;
    }

    private double clamp(double valor, double min, double max) {
        return Math.max(min, Math.min(max, valor));
    }
}
