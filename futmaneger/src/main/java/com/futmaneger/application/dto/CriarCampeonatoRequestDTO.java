package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.AbrangenciaCampeonato;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import java.math.BigDecimal;
import java.util.List;

public record CriarCampeonatoRequestDTO(
        String nome,
        String pais,
        String estado,
        AbrangenciaCampeonato abrangencia,
        BigDecimal premiacaoCampeao,
        BigDecimal premiacaoVice,
        Integer mesInicio,
        Integer mesFim,
        List<Long> clubes,
        CampeonatoEntity.TipoCampeonato tipo
) {
    public CriarCampeonatoRequestDTO(String nome, String pais, String estado,
                                     AbrangenciaCampeonato abrangencia,
                                     BigDecimal premiacaoCampeao, BigDecimal premiacaoVice,
                                     Integer mesInicio, Integer mesFim, List<Long> clubes) {
        this(nome, pais, estado, abrangencia, premiacaoCampeao, premiacaoVice,
                mesInicio, mesFim, clubes, null);
    }
}
