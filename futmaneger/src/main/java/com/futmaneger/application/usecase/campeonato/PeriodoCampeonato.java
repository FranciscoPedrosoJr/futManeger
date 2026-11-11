package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Regras do período (meses de início e fim) de um campeonato e de como ele vira
 * a janela de uma edição em determinado ano.
 */
public final class PeriodoCampeonato {

    private PeriodoCampeonato() {
    }

    /**
     * Aceita nenhum mês (a competição ocupa a temporada inteira) ou os dois,
     * entre 1 e 12 e em ordem.
     */
    public static void validar(Integer mesInicio, Integer mesFim) {
        if (mesInicio == null && mesFim == null) {
            return;
        }
        if (mesInicio == null || mesFim == null) {
            throw new DadosInvalidosException("Informe o mês de início e o mês de fim do campeonato, ou nenhum dos dois");
        }
        if (mesInicio < 1 || mesInicio > 12 || mesFim < 1 || mesFim > 12) {
            throw new DadosInvalidosException("Os meses do campeonato devem estar entre 1 e 12");
        }
        if (mesFim < mesInicio) {
            throw new DadosInvalidosException("O mês de fim do campeonato não pode ser anterior ao mês de início");
        }
    }

    /**
     * Resolve a janela da edição a partir dos meses do campeonato e do ano da
     * temporada. Sem meses configurados, a janela é limpa e a edição passa a
     * ocupar a temporada inteira.
     */
    public static void aplicarNaEdicao(EdicaoCampeonatoEntity edicao, CampeonatoEntity campeonato, int ano) {
        if (campeonato.getMesInicio() == null || campeonato.getMesFim() == null) {
            edicao.setDataInicioPrevista(null);
            edicao.setDataFimPrevista(null);
            return;
        }
        edicao.setDataInicioPrevista(LocalDate.of(ano, campeonato.getMesInicio(), 1));
        edicao.setDataFimPrevista(YearMonth.of(ano, campeonato.getMesFim()).atEndOfMonth());
    }
}
