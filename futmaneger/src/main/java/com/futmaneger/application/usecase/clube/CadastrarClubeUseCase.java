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
        ClubeEntity clube = new ClubeEntity(nome, estado, pais);
        return clubeJpaRepository.save(clube);
    }
}
