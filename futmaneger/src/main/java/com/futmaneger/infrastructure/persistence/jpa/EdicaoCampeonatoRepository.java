package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EdicaoCampeonatoRepository extends JpaRepository<EdicaoCampeonatoEntity, Long> {

    boolean existsByTemporadaAndCampeonato(TemporadaEntity temporada, CampeonatoEntity campeonato);

    List<EdicaoCampeonatoEntity> findByTemporada(TemporadaEntity temporada);

    List<EdicaoCampeonatoEntity> findByCampeonato(CampeonatoEntity campeonato);

    Optional<EdicaoCampeonatoEntity> findByTemporadaIdAndCampeonatoId(Long temporadaId, Long campeonatoId);
}
