package com.futmaneger.application.usecase.temporada;

import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdicionarCampeonatoNaTemporadaUseCase {

    private final TemporadaJpaRepository temporadaRepository;
    private final CampeonatoRepository campeonatoRepository;

    public AdicionarCampeonatoNaTemporadaUseCase(TemporadaJpaRepository temporadaRepository,
                                                 CampeonatoRepository campeonatoRepository) {
        this.temporadaRepository = temporadaRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    @Transactional
    public TemporadaResponseDTO executar(Long temporadaId, Long campeonatoId) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));

        CampeonatoEntity campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));

        if (campeonato.getTemporada() != null
                && !campeonato.getTemporada().getId().equals(temporadaId)) {
            throw new DadosInvalidosException("Campeonato já pertence a outra temporada");
        }

        temporada.adicionarCampeonato(campeonato);
        campeonatoRepository.save(campeonato);

        TemporadaEntity salva = temporadaRepository.save(temporada);
        return TemporadaMapper.toResponse(salva);
    }
}
