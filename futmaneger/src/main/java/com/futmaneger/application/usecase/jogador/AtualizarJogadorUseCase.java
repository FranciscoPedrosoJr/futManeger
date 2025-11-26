package com.futmaneger.application.usecase.jogador;

import com.futmaneger.application.dto.AtualizarJogadorRequestDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.antlr.v4.runtime.atn.SemanticContext;
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
            if(request.valorMercado() == null)
            jogador.setValorMercado(calcularValorMercado(request.forca()));
        }

        if(request.valorMercado() != null)
        jogador.setValorMercado(request.valorMercado());

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
        final double volatilidade = 0.15; // 15%

        BigDecimal minimo;
        BigDecimal maximo;

        if (forca <= 20) {
            minimo = BigDecimal.valueOf(0);
            maximo = BigDecimal.valueOf(1_000_000);
        } else if (forca <= 49) {
            minimo = BigDecimal.valueOf(1_000_000);
            maximo = BigDecimal.valueOf(10_000_000);
        } else if (forca <= 79) {
            minimo = BigDecimal.valueOf(10_000_000);
            maximo = BigDecimal.valueOf(50_000_000);
        } else {
            minimo = BigDecimal.valueOf(50_000_000);
            maximo = BigDecimal.valueOf(1_000_000_000);
        }

        int faixaMin = (forca <= 20 ? 0 :
                forca <= 49 ? 21 :
                        forca <= 79 ? 50 : 80);

        int faixaMax = (forca <= 20 ? 20 :
                forca <= 49 ? 49 :
                        forca <= 79 ? 79 : 99);

        BigDecimal proporcao = BigDecimal
                .valueOf(forca - faixaMin)
                .divide(BigDecimal.valueOf(faixaMax - faixaMin), 4, RoundingMode.HALF_UP);

        BigDecimal range = maximo.subtract(minimo);
        BigDecimal valorBase = minimo.add(range.multiply(proporcao));

        double variacao = (Math.random() * 2 - 1) * volatilidade;
        BigDecimal multiplicador = BigDecimal.valueOf(1 + variacao);

        BigDecimal valorFinal = valorBase.multiply(multiplicador);

        return valorFinal.setScale(0, RoundingMode.HALF_UP);
    }

}

