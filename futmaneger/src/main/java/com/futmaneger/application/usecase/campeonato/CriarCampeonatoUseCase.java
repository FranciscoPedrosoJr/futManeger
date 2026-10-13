package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.CampeonatoResponseDTO;
import com.futmaneger.application.dto.CriarCampeonatoRequestDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Cria o molde reutilizável de um campeonato ({@link CampeonatoEntity}). O molde
 * guarda apenas a definição da competição (nome, país, estado, tipo e quantidade
 * de clubes). Os participantes, rodadas e resultados pertencem a cada edição e
 * são criados quando o campeonato é associado a uma temporada.
 */
@Service
public class CriarCampeonatoUseCase {
    private final ClubeJpaRepository clubeRepository;
    private final CampeonatoRepository campeonatoRepository;

    public CriarCampeonatoUseCase(
            ClubeJpaRepository clubeRepository,
            CampeonatoRepository campeonatoRepository
    ) {
        this.clubeRepository = clubeRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    public CampeonatoResponseDTO executar(CriarCampeonatoRequestDTO request) {
        List<ClubeEntity> clubes;

        if (request.clubes() != null && !request.clubes().isEmpty()) {
            clubes = clubeRepository.findAllById(request.clubes());
        } else {
            clubes = buscarClubesPorLocalidade(request);
        }

        if (clubes.size() < 2) {
            throw new DadosInvalidosException("Número insuficiente de clubes para criar um campeonato");
        }

        CampeonatoEntity.TipoCampeonato tipo =
                isPotenciaDeDois(clubes.size())
                        ? CampeonatoEntity.TipoCampeonato.MATA_MATA
                        : CampeonatoEntity.TipoCampeonato.PONTOS_CORRIDOS;

        CampeonatoEntity campeonato = new CampeonatoEntity();
        campeonato.setNome(request.nome());
        campeonato.setEstado(request.estado());
        campeonato.setPais(request.pais());
        campeonato.setTipo(tipo);
        campeonato.setQuantidadeClubes(clubes.size());
        campeonato = campeonatoRepository.save(campeonato);

        List<String> nomesClubes = clubes.stream()
                .map(ClubeEntity::getNome)
                .toList();

        return new CampeonatoResponseDTO(
                campeonato.getId(),
                campeonato.getNome(),
                campeonato.getTipo().name(),
                nomesClubes
        );
    }

    private List<ClubeEntity> buscarClubesPorLocalidade(CriarCampeonatoRequestDTO request) {
        if (request.estado() != null && !request.estado().isBlank()) {
            return clubeRepository.findByEstado(request.estado());
        } else if (request.pais() != null && !request.pais().isBlank()) {
            return clubeRepository.findByPais(request.pais());
        }
        throw new DadosInvalidosException("É necessário informar país ou estado para criar o campeonato");
    }

    private boolean isPotenciaDeDois(int numero) {
        return numero > 0 && (numero & (numero - 1)) == 0 && numero <= 32;
    }
}
