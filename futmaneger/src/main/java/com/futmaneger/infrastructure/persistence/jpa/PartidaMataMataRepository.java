package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaMataMataRepository extends JpaRepository<PartidaMataMataEntity, Long> {
    boolean existsByCampeonato(CampeonatoEntity campeonato);
}
