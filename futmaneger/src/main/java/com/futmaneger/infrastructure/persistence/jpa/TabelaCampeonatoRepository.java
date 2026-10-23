package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TabelaCampeonatoRepository extends JpaRepository<TabelaCampeonatoEntity, Long> {
    Optional<TabelaCampeonatoEntity> findByEdicaoIdAndClubeId(Long edicaoId, Long clubeId);

    List<TabelaCampeonatoEntity> findByEdicaoOrderByPontosDescSaldoGolsDescGolsProDesc(EdicaoCampeonatoEntity edicao);

    List<TabelaCampeonatoEntity> findByGrupoOrderByPontosDesc(Long grupo_clubes);

    List<TabelaCampeonatoEntity> findByEdicaoIdOrderByPontosDesc(Long edicaoId);
}
