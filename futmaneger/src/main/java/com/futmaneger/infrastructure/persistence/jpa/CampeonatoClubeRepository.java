package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampeonatoClubeRepository  extends JpaRepository<CampeonatoClubeEntity, Long> {
    List<CampeonatoClubeEntity> findByCampeonato(CampeonatoEntity campeonato);
}
