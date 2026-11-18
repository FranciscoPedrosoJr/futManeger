package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ReservaCalendarioMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaCalendarioMataMataRepository
        extends JpaRepository<ReservaCalendarioMataMataEntity, Long> {

    List<ReservaCalendarioMataMataEntity> findByEdicaoOrderByOrdemAsc(EdicaoCampeonatoEntity edicao);

    List<ReservaCalendarioMataMataEntity> findByEdicao_Temporada(TemporadaEntity temporada);

    void deleteByEdicao(EdicaoCampeonatoEntity edicao);
}
