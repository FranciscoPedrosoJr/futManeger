package com.futmaneger.application.usecase.transferencias;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.financeiro.MovimentacaoFinanceiraService;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TransacaoFinanceiraEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.presentation.response.ComprarJogadorResponse;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComprarJogadorUseCase {
    private final ClubeJpaRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final MovimentacaoFinanceiraService movimentacaoFinanceira;

    public ComprarJogadorUseCase(ClubeJpaRepository clubeRepository,
                                 JogadorRepository jogadorRepository,
                                 MovimentacaoFinanceiraService movimentacaoFinanceira) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.movimentacaoFinanceira = movimentacaoFinanceira;
    }

    @Transactional
    public ComprarJogadorResponse executar(Long clubeId, Long jogadorId) {
        ClubeEntity clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new NaoEncontradoException("Jogador não encontrado"));

        BigDecimal preco = jogador.getValorMercado();
        if (preco == null) {
            throw new DadosInvalidosException("Parece que o valor deste jogador esta vazio ou nulo: " + jogador.getNome());
        }

        TransacaoFinanceiraEntity transacao = movimentacaoFinanceira.debitar(
                clube, preco, CategoriaTransacao.TRANSFERENCIA,
                "Compra do jogador: " + jogador.getNome(), jogador.getId());

        jogador.setClube(clube);
        jogadorRepository.save(jogador);

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
