package com.futmaneger.application.usecase.temporada;

import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.EdicaoCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdicionarCampeonatoNaTemporadaUseCase {

    private final TemporadaJpaRepository temporadaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final EdicaoCampeonatoRepository edicaoRepository;
    private final ClubeJpaRepository clubeRepository;
    private final ClubeParticipanteRepository participanteRepository;

    public AdicionarCampeonatoNaTemporadaUseCase(TemporadaJpaRepository temporadaRepository,
                                                 CampeonatoRepository campeonatoRepository,
                                                 EdicaoCampeonatoRepository edicaoRepository,
                                                 ClubeJpaRepository clubeRepository,
                                                 ClubeParticipanteRepository participanteRepository) {
        this.temporadaRepository = temporadaRepository;
        this.campeonatoRepository = campeonatoRepository;
        this.edicaoRepository = edicaoRepository;
        this.clubeRepository = clubeRepository;
        this.participanteRepository = participanteRepository;
    }

    /**
     * Cria uma nova edição do campeonato dentro da temporada, com seus próprios
     * participantes. O mesmo campeonato pode ser disputado em temporadas
     * diferentes, mas não pode ser associado mais de uma vez à mesma temporada.
     */
    @Transactional
    public TemporadaResponseDTO executar(Long temporadaId, Long campeonatoId) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));

        CampeonatoEntity campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));

        if (edicaoRepository.existsByTemporadaAndCampeonato(temporada, campeonato)) {
            throw new DadosInvalidosException(
                    "Este campeonato já está associado a esta temporada");
        }

        EdicaoCampeonatoEntity edicao = new EdicaoCampeonatoEntity(campeonato, temporada);
        temporada.adicionarEdicao(edicao);
        edicao = edicaoRepository.save(edicao);

        criarParticipantes(edicao);

        TemporadaEntity salva = temporadaRepository.save(temporada);
        return TemporadaMapper.toResponse(salva);
    }

    /**
     * Cria os participantes desta edição a partir dos clubes da localidade do
     * campeonato (molde). Cada edição tem seus próprios participantes.
     */
    private void criarParticipantes(EdicaoCampeonatoEntity edicao) {
        CampeonatoEntity campeonato = edicao.getCampeonato();
        List<ClubeEntity> clubes = resolverClubes(campeonato);

        for (ClubeEntity clube : clubes) {
            ClubeParticipanteEntity participante = new ClubeParticipanteEntity();
            participante.setEdicao(edicao);
            participante.setClube(clube);
            participanteRepository.save(participante);
        }
    }

    private List<ClubeEntity> resolverClubes(CampeonatoEntity campeonato) {
        if (campeonato.getEstado() != null && !campeonato.getEstado().isBlank()) {
            return clubeRepository.findByEstado(campeonato.getEstado());
        }
        if (campeonato.getPais() != null && !campeonato.getPais().isBlank()) {
            return clubeRepository.findByPais(campeonato.getPais());
        }
        throw new DadosInvalidosException(
                "Campeonato não possui localidade (estado/país) para definir os participantes da edição");
    }
}
