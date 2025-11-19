package com.futmaneger.infrastructure.persistence.impl;

import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import java.util.List;
import java.util.Optional;
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
    public List<Jogador> findByClube(ClubeEntity clube) {
        return jogadorJpaRepository.findByClube(clube);
    }

    @Override
    public void save(Jogador jogador) {
        jogadorJpaRepository.save(jogador);
    }

    @Override
    public Optional<Jogador> findById(Long id) {
        return jogadorJpaRepository.findById(id);
    }
}

