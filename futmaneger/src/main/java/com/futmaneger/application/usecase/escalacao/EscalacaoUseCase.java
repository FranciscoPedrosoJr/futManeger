package com.futmaneger.application.usecase.escalacao;

import com.futmaneger.application.dto.EscalacaoRequestDTO;
import com.futmaneger.application.dto.EscalacaoResponseDTO;
import com.futmaneger.application.dto.JogadorEscaladoDTO;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.EscalacaoEntity;
import com.futmaneger.infrastructure.persistence.entity.EscalacaoJogadorEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.EscalacaoJogadorRepository;
import com.futmaneger.infrastructure.persistence.jpa.EscalacaoRepository;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EscalacaoUseCase {

    private final ClubeJpaRepository clubeRepository;
    private final JogadorJpaRepository jogadorRepository;
    private final EscalacaoRepository escalacaoRepository;
    private final EscalacaoJogadorRepository escalacaoJogadorRepository;

    public EscalacaoUseCase(
            ClubeJpaRepository clubeRepository,
            JogadorJpaRepository jogadorRepository,
            EscalacaoRepository escalacaoRepository,
            EscalacaoJogadorRepository escalacaoJogadorRepository
    ) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.escalacaoRepository = escalacaoRepository;
        this.escalacaoJogadorRepository = escalacaoJogadorRepository;
    }

    public EscalacaoResponseDTO escalar(EscalacaoRequestDTO request) {
        var clube = clubeRepository.findById(request.clubeId())
                .orElseThrow(() -> new RuntimeException("Clube não encontrado"));

        var escalacao = new EscalacaoEntity();
        escalacao.setClube(clube);
        escalacao.setFormacao(request.formacao());
        escalacao.setDataHora(LocalDateTime.now());
        escalacao = escalacaoRepository.save(escalacao);

        List<Jogador> titulares = jogadorRepository.findAllById(request.titulares());
        List<Jogador> reservas = jogadorRepository.findAllById(request.reservas());

        for (Jogador j : titulares) {
            EscalacaoJogadorEntity ej = new EscalacaoJogadorEntity();
            ej.setEscalacao(escalacao);
            ej.setJogador(j);
            ej.setTipo(EscalacaoJogadorEntity.TipoJogador.TITULAR);
            escalacaoJogadorRepository.save(ej);
        }

        for (Jogador j : reservas) {
            EscalacaoJogadorEntity ej = new EscalacaoJogadorEntity();
            ej.setEscalacao(escalacao);
            ej.setJogador(j);
            ej.setTipo(EscalacaoJogadorEntity.TipoJogador.RESERVA);
            escalacaoJogadorRepository.save(ej);
        }

        List<JogadorEscaladoDTO> titularesDTO = titulares.stream()
                .map(j -> new JogadorEscaladoDTO(j.getNome(), j.getPosicao()))
                .toList();

        List<JogadorEscaladoDTO> reservasDTO = reservas.stream()
                .map(j -> new JogadorEscaladoDTO(j.getNome(), j.getPosicao()))
                .toList();

        return new EscalacaoResponseDTO(request.formacao(), titularesDTO, reservasDTO);
    }
}