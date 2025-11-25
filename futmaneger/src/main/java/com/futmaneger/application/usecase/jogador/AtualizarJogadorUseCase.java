package com.futmaneger.application.usecase.jogador;

import com.futmaneger.application.dto.AtualizarJogadorRequestDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class AtualizarJogadorUseCase {

    private final JogadorRepository jogadorRepository;
    private final JogadorJpaRepository jogadorJpaRepository;
    private final ClubeJpaRepository clubeJpaRepository;

    public AtualizarJogadorUseCase(JogadorRepository jogadorRepository,
                                   JogadorJpaRepository jogadorJpaRepository,
                                   ClubeJpaRepository clubeJpaRepository) {
        this.jogadorRepository = jogadorRepository;
        this.jogadorJpaRepository = jogadorJpaRepository;
        this.clubeJpaRepository = clubeJpaRepository;
    }

    public Jogador atualizar(Long jogadorId, AtualizarJogadorRequestDTO request) {

        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new NaoEncontradoException("Jogador não encontrado"));

        if (request.nome() != null) {
            jogador.setNome(request.nome());
        }

        if (request.posicao() != null) {
            jogador.setPosicao(request.posicao());
        }

        if (request.forca() != null) {
            jogador.setForca(request.forca());
            jogador.setValorMercado(calcularValorMercado(request.forca()));
        }

        if (request.diferenciado() != null) {
            jogador.setDiferenciado(request.diferenciado());
        }

        if (request.identificacaoComClube() != null) {
            jogador.setIdentificacaoComClube(request.identificacaoComClube());
        }

        if (request.clubeId() != null) {
            ClubeEntity clube = clubeJpaRepository.findById(request.clubeId())
                    .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

            jogador.setClube(clube);
        }

        return jogadorJpaRepository.save(jogador);
    }

    private BigDecimal calcularValorMercado(Integer forca) {
        if (forca < 50) return BigDecimal.valueOf(5_000_000);
        if (forca < 70) return BigDecimal.valueOf(20_000_000);
        return BigDecimal.valueOf(50_000_000);
    }
}

