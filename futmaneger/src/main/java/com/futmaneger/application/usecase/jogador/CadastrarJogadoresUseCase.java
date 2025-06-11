package com.futmaneger.application.usecase.jogador;

import com.futmaneger.application.dto.JogadorLoteRequestDTO;
import com.futmaneger.application.dto.JogadorResponseDTO;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
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
                    Clube clube = clubeRepository.findById(dto.clubeId())
                            .orElseThrow(() -> new RuntimeException("Clube não encontrado"));
                    return new Jogador(
                            dto.nome(),
                            dto.posicao(),
                            dto.forca(),
                            dto.diferenciado(),
                            dto.identificacaoComClube(),
                            clube
                    );
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
                        j.getClubeId().toString()
                )).toList();
    }
}