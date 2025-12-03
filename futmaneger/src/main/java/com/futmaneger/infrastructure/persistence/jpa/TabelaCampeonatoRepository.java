package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TabelaCampeonatoRepository extends JpaRepository<TabelaCampeonatoEntity, Long> {
    Optional<TabelaCampeonatoEntity> findByCampeonatoIdAndClubeId(Long campeonatoId, Long clubeId);

    List<TabelaCampeonatoEntity> findByCampeonatoOrderByPontosDescSaldoGolsDescGolsProDesc(CampeonatoEntity campeonato);

    List<TabelaCampeonatoEntity> findByGrupoOrderByPontosDesc(Long grupo_clubes);

    List<TabelaCampeonatoEntity> findByCampeonatoIdOrderByPontosDesc(Long campeonatoId);
}

