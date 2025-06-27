package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.CampeonatoResponseDTO;
import com.futmaneger.application.dto.CriarCampeonatoRequestDTO;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CriarCampeonatoUseCase {
    private final ClubeJpaRepository clubeRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final ClubeParticipanteRepository participanteRepository;

    public CriarCampeonatoUseCase(
            ClubeJpaRepository clubeRepository,
            CampeonatoRepository campeonatoRepository,
            ClubeParticipanteRepository participanteRepository
    ) {
        this.clubeRepository = clubeRepository;
        this.campeonatoRepository = campeonatoRepository;
        this.participanteRepository = participanteRepository;
    }

    public CampeonatoResponseDTO executar(CriarCampeonatoRequestDTO request) {
        List<Clube> clubes = buscarClubesPorLocalidade(request);

        if (clubes.size() < 2) {
            throw new IllegalArgumentException("Número insuficiente de clubes para criar um campeonato");
        }

        CampeonatoEntity.TipoCampeonato tipo = clubes.size() > 20 ? CampeonatoEntity.TipoCampeonato.MATA_MATA : CampeonatoEntity.TipoCampeonato.PONTOS_CORRIDOS;

        CampeonatoEntity campeonato = new CampeonatoEntity();
        campeonato.setNome(request.nome());
        campeonato.setEstado(request.estado());
        campeonato.setPais(request.pais());
        campeonato.setTipo(tipo);
        campeonato = campeonatoRepository.save(campeonato);

        for (Clube clube : clubes) {
            ClubeParticipanteEntity participante = new ClubeParticipanteEntity();
            participante.setCampeonato(campeonato);
            participante.setClube(clube);
            participanteRepository.save(participante);
        }

        List<String> nomesClubes = clubes.stream()
                .map(Clube::getNome)
                .toList();

        return new CampeonatoResponseDTO(
                campeonato.getId(),
                campeonato.getNome(),
                campeonato.getTipo().name(),
                nomesClubes
        );
    }

    private List<Clube> buscarClubesPorLocalidade(CriarCampeonatoRequestDTO request) {
        if (request.estado() != null && !request.estado().isBlank()) {
            return clubeRepository.findByEstado(request.estado());
        } else if (request.pais() != null && !request.pais().isBlank()) {
            return clubeRepository.findByPais(request.pais());
        }
        throw new IllegalArgumentException("É necessário informar país ou estado para criar o campeonato");
    }
}