package com.futmaneger.application.usecase.clube;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class CadastrarClubeUseCase {
    private final ClubeJpaRepository clubeJpaRepository;

    public CadastrarClubeUseCase(ClubeJpaRepository clubeJpaRepository) {
        this.clubeJpaRepository = clubeJpaRepository;
    }

    public ClubeEntity executar(String nome, String estado, String pais) {
        return executar(nome, estado, pais, false);
    }

    public ClubeEntity executar(String nome, String estado, String pais, boolean ehSelecao) {
        return executar(nome, estado, pais, ehSelecao, null);
    }

    public ClubeEntity executar(String nome, String estado, String pais, boolean ehSelecao,
                                java.math.BigDecimal precoIngresso) {
        ClubeEntity clube = new ClubeEntity(nome, estado, pais, ehSelecao);
        if (precoIngresso != null) {
            clube.setPrecoIngresso(precoIngresso);
        }
        return clubeJpaRepository.save(clube);
    }
}
