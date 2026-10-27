package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.CotaEstadualEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotaEstadualRepository extends JpaRepository<CotaEstadualEntity, Long> {

    List<CotaEstadualEntity> findByCampeonato(CampeonatoEntity campeonato);

    boolean existsByCampeonatoAndEstado(CampeonatoEntity campeonato, String estado);
}
