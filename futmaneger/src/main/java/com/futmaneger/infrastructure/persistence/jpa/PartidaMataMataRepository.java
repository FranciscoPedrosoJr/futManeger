package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaMataMataRepository extends JpaRepository<PartidaMataMataEntity, Long> {
    boolean existsByCampeonato(CampeonatoEntity campeonato);

    List<PartidaMataMataEntity> findByCampeonatoId(Long campeonatoId);
    List<PartidaMataMataEntity> findByRodada(RodadaEntity rodada);

    boolean existsByCampeonatoAndFase(CampeonatoEntity campeonato, PartidaMataMataEntity.FaseMataMata fase);

    @Query("SELECT p FROM PartidaMataMataEntity p WHERE p.rodada.id = :rodadaId")
    List<PartidaMataMataEntity> findByRodadaId(@Param("rodadaId") Long rodadaId);

}
