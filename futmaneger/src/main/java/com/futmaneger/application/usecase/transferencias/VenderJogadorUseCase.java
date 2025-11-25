package com.futmaneger.application.usecase.transferencias;

import com.futmaneger.application.dto.VendaJogadorResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.HistoricoTransferenciaEntity;
import com.futmaneger.infrastructure.persistence.entity.TransacaoFinanceiraEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.HistoricoTransferenciaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TransacaoFinanceiraRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VenderJogadorUseCase {
    private final JogadorRepository jogadorRepository;
    private final ClubeJpaRepository clubeRepository;
    private final HistoricoTransferenciaRepository historicoRepository;
    private final TransacaoFinanceiraRepository transacaoFinanceiraRepository;

    public VenderJogadorUseCase(
            JogadorRepository jogadorRepository,
            ClubeJpaRepository clubeRepository,
            HistoricoTransferenciaRepository historicoRepository,
            TransacaoFinanceiraRepository transacaoFinanceiraRepository
    ) {
        this.jogadorRepository = jogadorRepository;
        this.clubeRepository = clubeRepository;
        this.historicoRepository = historicoRepository;
        this.transacaoFinanceiraRepository = transacaoFinanceiraRepository;
    }

    public VendaJogadorResponseDTO executar(Long jogadorId) {

        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new NaoEncontradoException("Jogador não encontrado"));

        ClubeEntity clubeVendedor = jogador.getClube();
        if (clubeVendedor == null) throw new DadosInvalidosException("Jogador não possui clube vinculado");

        BigDecimal precoJogador = calcularPreco(jogador);

        List<ClubeEntity> clubesPossiveis = clubeRepository.findAll().stream()
                .filter(c -> !c.getId().equals(clubeVendedor.getId()))
                .filter(c -> c.getSaldo().compareTo(precoJogador) >= 0)
                .filter(c -> mediaProxima(jogador, c))
                .toList();

        if (clubesPossiveis.isEmpty()) {
            throw new DadosInvalidosException("Nenhum clube elegível para comprar este jogador");
        }

        ClubeEntity clubeComprador = clubesPossiveis.stream()
                .max(Comparator.comparing(c -> necessidade(jogador, c)))
                .orElseThrow();

        String motivo = "Jogador se enquadra no perfil do clube";

        clubeComprador.setSaldo(clubeComprador.getSaldo().subtract(precoJogador));
        clubeVendedor.setSaldo(clubeVendedor.getSaldo().add(precoJogador));

        clubeRepository.save(clubeComprador);
        clubeRepository.save(clubeVendedor);

        jogador.setClube(clubeComprador);
        jogadorRepository.save(jogador);

        HistoricoTransferenciaEntity h = new HistoricoTransferenciaEntity();
        h.setJogadorId(jogador.getId());
        h.setClubeVendedorId(clubeVendedor.getId());
        h.setClubeCompradorId(clubeComprador.getId());
        h.setValor(precoJogador);
        h.setData(LocalDateTime.now());
        historicoRepository.save(h);

        registrarTransacoesTransferencia(jogador, clubeComprador, clubeVendedor, precoJogador);

        return new VendaJogadorResponseDTO(
                jogador.getId(),
                jogador.getNome(),
                clubeVendedor.getNome(),
                clubeComprador.getNome(),
                precoJogador,
                motivo
        );
    }

    private BigDecimal calcularPreco(Jogador jogador) {
        BigDecimal valorDeMercado = jogador.getValorMercado();
        return valorDeMercado;
    }

    private double mediaElenco(ClubeEntity clube) {
        Double media = clubeRepository.calcularMediaForca(clube.getId());
        return media != null ? media : 0.0;
    }

    private boolean mediaProxima(Jogador jogador, ClubeEntity clube) {
        double media = mediaElenco(clube);
        return Math.abs(media - jogador.getForca()) <= 10;
    }

    private double necessidade(Jogador jogador, ClubeEntity clube) {
        return jogador.getForca() - mediaElenco(clube);
    }

    private void registrarTransacoesTransferencia(Jogador jogador,
                                                  ClubeEntity clubeComprador,
                                                  ClubeEntity clubeVendedor,
                                                  BigDecimal precoJogador) {

        TransacaoFinanceiraEntity compra = new TransacaoFinanceiraEntity();
        compra.setJogadorId(jogador.getId());
        compra.setClubeId(clubeComprador.getId());
        compra.setValor(precoJogador.negate());
        compra.setDescricao("Compra do jogador: " + jogador.getNome());
        compra.setData(LocalDateTime.now());
        transacaoFinanceiraRepository.save(compra);

        TransacaoFinanceiraEntity venda = new TransacaoFinanceiraEntity();
        venda.setJogadorId(jogador.getId());
        venda.setClubeId(clubeVendedor.getId());
        venda.setValor(precoJogador);
        venda.setDescricao("Venda do jogador: " + jogador.getNome());
        venda.setData(LocalDateTime.now());
        transacaoFinanceiraRepository.save(venda);
    }

}
