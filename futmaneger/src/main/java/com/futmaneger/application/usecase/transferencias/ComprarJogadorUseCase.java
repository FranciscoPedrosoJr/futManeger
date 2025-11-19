package com.futmaneger.application.usecase.transferencias;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TransacaoFinanceiraEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TransacaoFinanceiraRepository;
import com.futmaneger.presentation.response.ComprarJogadorResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service

public class ComprarJogadorUseCase {
    private final ClubeJpaRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final TransacaoFinanceiraRepository transacaoRepository;

    public ComprarJogadorUseCase(ClubeJpaRepository clubeRepository,
                                 JogadorRepository jogadorRepository,
                                 TransacaoFinanceiraRepository transacaoRepository) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.transacaoRepository = transacaoRepository;
    }

    public ComprarJogadorResponse executar(Long clubeId, Long jogadorId) {

        ClubeEntity clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new NaoEncontradoException("Jogador não encontrado"));


        BigDecimal preco = jogador.getValorMercado();
        if(preco == null){
            throw new DadosInvalidosException("Parece que o valor deste jogador esta vazio ou nulo: "+ jogador.getNome());
        }

        if (clube.getSaldo().compareTo(preco) < 0 || clube.getSaldo() == null) {
            throw new DadosInvalidosException("Saldo insuficiente para contratar o jogador");
        }

        clube.setSaldo(clube.getSaldo().subtract(preco));
        clubeRepository.save(clube);

        jogador.setClube(clube);
        jogadorRepository.save(jogador);

        TransacaoFinanceiraEntity transacao = new TransacaoFinanceiraEntity();
        transacao.setClubeId(clube.getId());
        transacao.setJogadorId(jogador.getId());
        transacao.setValor(preco.negate());
        transacao.setDescricao("Compra do jogador: " + jogador.getNome());
        transacao.setData(LocalDateTime.now());

        transacaoRepository.save(transacao);
        return new ComprarJogadorResponse(
                transacao.getId(),
                transacao.getClubeId(),
                transacao.getJogadorId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getData()
        );
    }
}
