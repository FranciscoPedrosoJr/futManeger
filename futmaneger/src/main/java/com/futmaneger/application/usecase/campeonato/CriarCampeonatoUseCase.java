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
 * guarda a definição da competição (nome, país, estado, abrangência, formato de
 * disputa e quantidade de clubes). Os participantes, rodadas e resultados
 * pertencem a cada edição e são criados quando o campeonato é associado a uma
 * temporada.
 */
@Service
public class CriarCampeonatoUseCase {
    private final ClubeJpaRepository clubeRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final ResolverParticipantesPorAbrangenciaService resolverParticipantes;

    public CriarCampeonatoUseCase(
            ClubeJpaRepository clubeRepository,
            CampeonatoRepository campeonatoRepository,
            ResolverParticipantesPorAbrangenciaService resolverParticipantes
    ) {
        this.clubeRepository = clubeRepository;
        this.campeonatoRepository = campeonatoRepository;
        this.resolverParticipantes = resolverParticipantes;
    }

    public CampeonatoResponseDTO executar(CriarCampeonatoRequestDTO request) {
        if (request.abrangencia() == null) {
            throw new DadosInvalidosException("É necessário informar a abrangência do campeonato");
        }

        CampeonatoEntity campeonato = new CampeonatoEntity();
        campeonato.setNome(request.nome());
        campeonato.setEstado(request.estado());
        campeonato.setPais(request.pais());
        campeonato.setAbrangencia(request.abrangencia());
        campeonato.setPremiacaoCampeao(request.premiacaoCampeao());
        campeonato.setPremiacaoVice(request.premiacaoVice());

        List<ClubeEntity> clubes = resolverClubes(request, campeonato);

        if (clubes.size() < 2) {
            throw new DadosInvalidosException("Número insuficiente de clubes para criar um campeonato");
        }

        CampeonatoEntity.TipoCampeonato formato =
                isPotenciaDeDois(clubes.size())
                        ? CampeonatoEntity.TipoCampeonato.MATA_MATA
                        : CampeonatoEntity.TipoCampeonato.PONTOS_CORRIDOS;

        campeonato.setTipo(formato);
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

    /**
     * Quando o request informa clubes explicitamente, usa-os; caso contrário,
     * resolve automaticamente pelos critérios da abrangência do campeonato.
     */
    private List<ClubeEntity> resolverClubes(CriarCampeonatoRequestDTO request, CampeonatoEntity campeonato) {
        if (request.clubes() != null && !request.clubes().isEmpty()) {
            List<Long> ids = request.clubes().stream().distinct().toList();
            List<ClubeEntity> clubes = clubeRepository.findAllById(ids);
            if (clubes.size() != ids.size()) {
                throw new DadosInvalidosException("Um ou mais clubes informados não foram encontrados");
            }
            return clubes;
        }
        return resolverParticipantes.resolver(campeonato);
    }

    private boolean isPotenciaDeDois(int numero) {
        return numero > 0 && (numero & (numero - 1)) == 0 && numero <= 32;
    }
}
