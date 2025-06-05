package com.futmaneger.application.usecase.escalacao;

import com.futmaneger.application.dto.EscalacaoRequestDTO;
import com.futmaneger.application.dto.EscalacaoResponseDTO;
import com.futmaneger.application.dto.JogadorEscaladoDTO;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class EscalacaoUseCase {

    private final JogadorJpaRepository jogadorRepository;

    public EscalacaoUseCase(JogadorJpaRepository jogadorRepository) {
        this.jogadorRepository = jogadorRepository;
    }

    public EscalacaoResponseDTO escalar(EscalacaoRequestDTO request) {
        List<JogadorEscaladoDTO> titulares = jogadorRepository.findAllById(request.titulares())
                .stream()
                .map(j -> new JogadorEscaladoDTO(j.getNome(), j.getPosicao()))
                .collect(Collectors.toList());

        List<JogadorEscaladoDTO> reservas = jogadorRepository.findAllById(request.reservas())
                .stream()
                .map(j -> new JogadorEscaladoDTO(j.getNome(), j.getPosicao()))
                .collect(Collectors.toList());

        return new EscalacaoResponseDTO(request.formacao(), titulares, reservas);
    }
}

