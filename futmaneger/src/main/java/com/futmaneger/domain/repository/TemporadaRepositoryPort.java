package com.futmaneger.domain.repository;

import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import java.util.List;
import java.util.Optional;

public interface TemporadaRepositoryPort {

    TemporadaEntity salvar(TemporadaEntity temporada);

    Optional<TemporadaEntity> buscarPorId(Long id);

    Optional<TemporadaEntity> buscarPorAno(Integer ano);

    List<TemporadaEntity> listarTodas();
}
