package com.futmaneger.application.usecase.financeiro;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TransacaoFinanceiraEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TransacaoFinanceiraRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimentacaoFinanceiraService {

    private final ClubeJpaRepository clubeRepository;
    private final TransacaoFinanceiraRepository transacaoRepository;

    public MovimentacaoFinanceiraService(ClubeJpaRepository clubeRepository,
                                         TransacaoFinanceiraRepository transacaoRepository) {
        this.clubeRepository = clubeRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public TransacaoFinanceiraEntity creditar(ClubeEntity clube, BigDecimal valor,
                                              CategoriaTransacao categoria, String descricao, Long jogadorId) {
        validarValorPositivo(valor);
        return registrar(clube, valor, categoria, descricao, jogadorId);
    }

    @Transactional
    public TransacaoFinanceiraEntity debitar(ClubeEntity clube, BigDecimal valor,
                                             CategoriaTransacao categoria, String descricao, Long jogadorId) {
        validarValorPositivo(valor);

        BigDecimal saldoAtual = clube.getSaldo() == null ? BigDecimal.ZERO : clube.getSaldo();
        if (saldoAtual.compareTo(valor) < 0) {
            throw new DadosInvalidosException(
                    "Saldo insuficiente no clube " + clube.getNome() + " para debitar " + valor);
        }

        return registrar(clube, valor.negate(), categoria, descricao, jogadorId);
    }

    @Transactional
    public TransacaoFinanceiraEntity debitarPermitindoNegativo(ClubeEntity clube, BigDecimal valor,
                                                               CategoriaTransacao categoria, String descricao,
                                                               Long jogadorId) {
        validarValorPositivo(valor);
        return registrar(clube, valor.negate(), categoria, descricao, jogadorId);
    }

    @Transactional
    public TransacaoFinanceiraEntity ajustar(ClubeEntity clube, BigDecimal valor, String descricao) {
        if (valor == null) {
            throw new DadosInvalidosException("O valor do ajuste é obrigatório");
        }
        BigDecimal saldoAtual = clube.getSaldo() == null ? BigDecimal.ZERO : clube.getSaldo();
        if (saldoAtual.add(valor).compareTo(BigDecimal.ZERO) < 0) {
            throw new DadosInvalidosException(
                    "O ajuste deixaria o saldo do clube " + clube.getNome() + " negativo");
        }
        return registrar(clube, valor, CategoriaTransacao.AJUSTE, descricao, null);
    }

    private TransacaoFinanceiraEntity registrar(ClubeEntity clube, BigDecimal valorComSinal,
                                                CategoriaTransacao categoria, String descricao, Long jogadorId) {
        BigDecimal saldoAtual = clube.getSaldo() == null ? BigDecimal.ZERO : clube.getSaldo();
        clube.setSaldo(saldoAtual.add(valorComSinal));
        clubeRepository.save(clube);

        TransacaoFinanceiraEntity transacao = new TransacaoFinanceiraEntity();
        transacao.setClubeId(clube.getId());
        transacao.setJogadorId(jogadorId);
        transacao.setValor(valorComSinal);
        transacao.setCategoria(categoria);
        transacao.setDescricao(descricao);
        transacao.setData(LocalDateTime.now());
        return transacaoRepository.save(transacao);
    }

    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DadosInvalidosException("O valor da movimentação deve ser maior que zero");
        }
    }
}
