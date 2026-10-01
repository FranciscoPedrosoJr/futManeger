package com.futmaneger.infrastructure.persistence.impl;

import com.futmaneger.domain.repository.TemporadaRepositoryPort;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TemporadaRepositoryImpl implements TemporadaRepositoryPort {

    private final TemporadaJpaRepository temporadaRepository;

    @Override
    public TemporadaEntity salvar(TemporadaEntity temporada) {
        return temporadaRepository.save(temporada);
    }

    @Override
    public Optional<TemporadaEntity> buscarPorId(Long id) {
        return temporadaRepository.findById(id);
    }

    @Override
    public Optional<TemporadaEntity> buscarPorAno(Integer ano) {
        return temporadaRepository.findByAno(ano);
    }

    @Override
    public List<TemporadaEntity> listarTodas() {
        return temporadaRepository.findAll();
    }
}
