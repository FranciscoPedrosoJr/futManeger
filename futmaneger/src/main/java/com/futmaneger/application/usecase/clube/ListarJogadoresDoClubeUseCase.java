package com.futmaneger.application.usecase.clube;

import com.futmaneger.application.dto.JogadoresDoClubeResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarJogadoresDoClubeUseCase {

    private final ClubeJpaRepository clubeRepository;
    private final JogadorJpaRepository jogadorRepository;

    public JogadoresDoClubeResponseDTO executar(Long clubeId) {

        var clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

        var jogadores = jogadorRepository.findByClube(clube);

        var jogadoresDTO = jogadores.stream()
                .map(j -> new JogadoresDoClubeResponseDTO.JogadorDTO(
                        j.getId(),
                        j.getNome(),
                        j.getPosicao(),
                        j.getForca(),
                        j.isDiferenciado(),
                        j.isIdentificacaoComClube()
                ))
                .toList();

        return new JogadoresDoClubeResponseDTO(
                clube.getId(),
                clube.getNome(),
                jogadoresDTO
        );
    }
}

