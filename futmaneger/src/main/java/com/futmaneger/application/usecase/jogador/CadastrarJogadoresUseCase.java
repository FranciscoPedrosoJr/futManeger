package com.futmaneger.application.usecase.jogador;

import static java.util.stream.Collectors.toList;

import com.futmaneger.application.dto.JogadorLoteRequestDTO;
import com.futmaneger.application.dto.JogadorResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Clube;
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
                    jogador.setValorMercado(definirValorMercado(dto.forca()));
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

    private BigDecimal definirValorMercado(int forca) {

        if (forca <= 50) {
            return gerarValor(new BigDecimal("5000000"), new BigDecimal("20000000"));
        }

        if (forca <= 70) {
            return gerarValor(new BigDecimal("20000000"), new BigDecimal("50000000"));
        }

        return gerarValor(new BigDecimal("50000000"), new BigDecimal("100000000"));
    }

    private BigDecimal gerarValor(BigDecimal min, BigDecimal max) {
        double minD = min.doubleValue();
        double maxD = max.doubleValue();

        double aleatorio = minD + (Math.random() * (maxD - minD));

        return BigDecimal.valueOf(aleatorio).setScale(0, RoundingMode.HALF_UP);
    }
}