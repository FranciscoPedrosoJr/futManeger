package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaMataMataRepository extends JpaRepository<PartidaMataMataEntity, Long> {
    boolean existsByEdicao(EdicaoCampeonatoEntity edicao);

    List<PartidaMataMataEntity> findByEdicaoId(Long edicaoId);
    List<PartidaMataMataEntity> findByRodada(RodadaEntity rodada);

    boolean existsByEdicaoAndFase(EdicaoCampeonatoEntity edicao, PartidaMataMataEntity.FaseMataMata fase);

    @Query("SELECT p FROM PartidaMataMataEntity p WHERE p.rodada.id = :rodadaId")
    List<PartidaMataMataEntity> findByRodadaId(@Param("rodadaId") Long rodadaId);

}
