package com.futmaneger.application.usecase.jogador;

import com.futmaneger.application.dto.JogadorLoteRequestDTO;
import com.futmaneger.application.dto.JogadorResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CadastrarJogadoresUseCase {
    private final JogadorRepository jogadorRepository;
    private final ClubeJpaRepository clubeRepository;

    public CadastrarJogadoresUseCase(JogadorRepository jogadorRepository, ClubeJpaRepository clubeRepository) {
        this.jogadorRepository = jogadorRepository;
        this.clubeRepository = clubeRepository;
    }

    public List<JogadorResponseDTO> cadastrarLote(JogadorLoteRequestDTO request) {
        List<Jogador> jogadores = request.jogadores().stream()
                .map(dto -> {
                    ClubeEntity clube = clubeRepository.findById(dto.clubeId())
                            .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));
                    Jogador jogador = new Jogador(
                            dto.nome(),
                            dto.posicao(),
                            dto.forca(),
                            dto.diferenciado(),
                            dto.identificacaoComClube(),
                            clube
                    );
                    jogador.setValorMercado(calcularValorMercado(dto.forca()));
                    return jogador;
                }).toList();

        jogadorRepository.saveAll(jogadores);

        return jogadores.stream()
                .map(j -> new JogadorResponseDTO(
                        j.getId(),
                        j.getNome(),
                        j.getPosicao(),
                        j.getForca(),
                        j.isDiferenciado(),
                        j.isIdentificacaoComClube(),
                        j.getClubeId().toString(),
                        j.getValorMercado()
                )).toList();
    }

    private BigDecimal calcularValorMercado(int forca) {

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