package com.futmaneger.application.usecase.vinculo;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DesvincularJogadorDoClubeUseCase {
    private final ClubeJpaRepository clubeJpaRepository;
    private final JogadorJpaRepository jogadorJpaRepository;

    public DesvincularJogadorDoClubeUseCase(ClubeJpaRepository clubeJpaRepository, JogadorJpaRepository jogadorJpaRepository) {
        this.clubeJpaRepository = clubeJpaRepository;
        this.jogadorJpaRepository = jogadorJpaRepository;
    }

    public void desvincular(Long clubeId, Long jogadorId) {
        ClubeEntity clube = clubeJpaRepository.findById(clubeId)
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

        Jogador jogador = jogadorJpaRepository.findById(jogadorId)
                .orElseThrow(() -> new NaoEncontradoException("Jogador não encontrado"));

        if (!jogador.getClube().getId().equals(clubeId)) {
            throw new DadosInvalidosException("O jogador não está vinculado a este clube");
        }

        jogador.setClube(null);
        jogadorJpaRepository.save(jogador);
    }

}
