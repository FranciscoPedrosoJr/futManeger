package com.futmaneger.application.usecase.jogador;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ExcluirJogadorUseCase {
    private final JogadorJpaRepository jogadorRepository;

    public ExcluirJogadorUseCase(JogadorJpaRepository jogadorRepository) {
        this.jogadorRepository = jogadorRepository;
    }

    public void excluirJogador(Long jogadorId) {
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new NaoEncontradoException("Jogador não encontrado"));

        if (jogador.getClube() != null) {
            throw new DadosInvalidosException("O jogador está vinculado a um clube, exclua o vinculo e depois tente novamente!");
        }

        jogadorRepository.delete(jogador);
    }
}