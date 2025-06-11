package com.futmaneger.infrastructure.persistence.impl;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class JogadorRepositoryImpl implements JogadorRepository {
    private final JogadorJpaRepository jogadorJpaRepository;

    public JogadorRepositoryImpl(JogadorJpaRepository jogadorJpaRepository) {
        this.jogadorJpaRepository = jogadorJpaRepository;
    }

    @Override
    public void saveAll(List<Jogador> jogadores) {
        jogadorJpaRepository.saveAll(jogadores);
    }

    @Override
    public List<Jogador> findByClube(Clube clube) {
        return jogadorJpaRepository.findByClube(clube);
    }
}

