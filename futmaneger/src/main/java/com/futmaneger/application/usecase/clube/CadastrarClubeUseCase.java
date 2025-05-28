package com.futmaneger.application.usecase.clube;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class CadastrarClubeUseCase {
    private final ClubeJpaRepository clubeJpaRepository;

    public CadastrarClubeUseCase(ClubeJpaRepository clubeJpaRepository) {
        this.clubeJpaRepository = clubeJpaRepository;
    }

    public Clube executar(String nome, String cidade) {
        Clube clube = new Clube(nome, cidade);
        return clubeJpaRepository.save(clube);
    }
}
