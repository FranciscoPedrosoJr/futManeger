package com.futmaneger.application.usecase.temporada;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.campeonato.ResolverParticipantesPorAbrangenciaService;
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

@Service
public class AdicionarCampeonatoNaTemporadaUseCase {

    private final TemporadaJpaRepository temporadaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final EdicaoCampeonatoRepository edicaoRepository;
    private final ClubeJpaRepository clubeRepository;
    private final ClubeParticipanteRepository participanteRepository;
    private final ResolverParticipantesPorAbrangenciaService resolverParticipantes;

    public AdicionarCampeonatoNaTemporadaUseCase(TemporadaJpaRepository temporadaRepository,
                                                 CampeonatoRepository campeonatoRepository,
                                                 EdicaoCampeonatoRepository edicaoRepository,
                                                 ClubeJpaRepository clubeRepository,
                                                 ClubeParticipanteRepository participanteRepository,
                                                 ResolverParticipantesPorAbrangenciaService resolverParticipantes) {
        this.temporadaRepository = temporadaRepository;
        this.campeonatoRepository = campeonatoRepository;
        this.edicaoRepository = edicaoRepository;
        this.clubeRepository = clubeRepository;
        this.participanteRepository = participanteRepository;
        this.resolverParticipantes = resolverParticipantes;
    }

    /**
     * Cria uma nova edição do campeonato dentro da temporada selecionando os
     * participantes automaticamente a partir dos clubes da localidade do
     * campeonato (molde), respeitando a quantidade de clubes definida no cadastro.
     *
     * <p>O mesmo campeonato pode ser disputado em temporadas diferentes, mas não
     * pode ser associado mais de uma vez à mesma temporada.</p>
     */
    @Transactional
    public TemporadaResponseDTO executar(Long temporadaId, Long campeonatoId) {
        return executar(temporadaId, campeonatoId, null);
    }

    /**
     * Cria uma nova edição do campeonato dentro da temporada.
     *
     * <p>Quando {@code clubesIds} é {@code null} ou vazio, os participantes são
     * selecionados automaticamente a partir da localidade do campeonato. Quando
     * informado, apenas os clubes indicados participam da edição.</p>
     *
     * <p>Em ambos os casos a quantidade de participantes deve ser igual à
     * {@code quantidadeClubes} definida no cadastro do campeonato.</p>
     */
    @Transactional
    public TemporadaResponseDTO executar(Long temporadaId, Long campeonatoId, List<Long> clubesIds) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));

        CampeonatoEntity campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));

        if (edicaoRepository.existsByTemporadaAndCampeonato(temporada, campeonato)) {
            throw new DadosInvalidosException(
                    "Este campeonato já está associado a esta temporada");
        }

        List<ClubeEntity> clubes = (clubesIds == null || clubesIds.isEmpty())
                ? resolverClubesAutomaticamente(campeonato)
                : resolverClubesPorIds(clubesIds);

        validarQuantidade(campeonato, clubes);

        EdicaoCampeonatoEntity edicao = new EdicaoCampeonatoEntity(campeonato, temporada);
        temporada.adicionarEdicao(edicao);
        edicao = edicaoRepository.save(edicao);

        criarParticipantes(edicao, clubes);

        TemporadaEntity salva = temporadaRepository.save(temporada);
        return TemporadaMapper.toResponse(salva);
    }

    /**
     * Cria os participantes da edição a partir da lista de clubes já resolvida.
     * Cada edição tem seus próprios participantes.
     */
    private void criarParticipantes(EdicaoCampeonatoEntity edicao, List<ClubeEntity> clubes) {
        for (ClubeEntity clube : clubes) {
            ClubeParticipanteEntity participante = new ClubeParticipanteEntity();
            participante.setEdicao(edicao);
            participante.setClube(clube);
            participanteRepository.save(participante);
        }
    }

    /**
     * Seleciona automaticamente os clubes elegíveis segundo a abrangência do
     * campeonato, limitando à quantidade de clubes definida no cadastro.
     */
    private List<ClubeEntity> resolverClubesAutomaticamente(CampeonatoEntity campeonato) {
        List<ClubeEntity> disponiveis = resolverParticipantes.resolver(campeonato);

        int quantidade = campeonato.getQuantidadeClubes();
        if (disponiveis.size() < quantidade) {
            throw new DadosInvalidosException(
                    "A abrangência do campeonato disponibiliza apenas " + disponiveis.size()
                            + " clube(s), mas o campeonato exige " + quantidade + ".");
        }

        return disponiveis.subList(0, quantidade);
    }

    /**
     * Resolve os clubes informados explicitamente por id, garantindo que não há
     * ids duplicados e que todos existem.
     */
    private List<ClubeEntity> resolverClubesPorIds(List<Long> clubesIds) {
        List<Long> idsUnicos = clubesIds.stream().distinct().toList();
        if (idsUnicos.size() != clubesIds.size()) {
            throw new DadosInvalidosException("A lista de clubes contém ids duplicados.");
        }

        List<ClubeEntity> clubes = clubeRepository.findAllById(idsUnicos);
        if (clubes.size() != idsUnicos.size()) {
            Set<Long> encontrados = clubes.stream().map(ClubeEntity::getId).collect(Collectors.toSet());
            List<Long> inexistentes = idsUnicos.stream()
                    .filter(id -> !encontrados.contains(id))
                    .toList();
            throw new NaoEncontradoException("Clube(s) não encontrado(s): " + inexistentes);
        }

        return clubes;
    }

    /**
     * Garante que o número de clubes participantes é exatamente o definido no
     * cadastro do campeonato.
     */
    private void validarQuantidade(CampeonatoEntity campeonato, List<ClubeEntity> clubes) {
        int quantidade = campeonato.getQuantidadeClubes();
        if (clubes.size() != quantidade) {
            throw new DadosInvalidosException(
                    "O campeonato exige " + quantidade + " clube(s), mas foram informados "
                            + clubes.size() + ".");
        }
    }
}
