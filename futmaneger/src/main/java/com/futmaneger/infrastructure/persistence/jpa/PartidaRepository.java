package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaFaseDeGruposEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaRepository extends JpaRepository<PartidaEntity, Long> {
    List<PartidaFaseDeGruposEntity> findByCampeonato(CampeonatoEntity campeonato);

    List<PartidaEntity> findPartidasByCampeonato(CampeonatoEntity campeonato);
}
