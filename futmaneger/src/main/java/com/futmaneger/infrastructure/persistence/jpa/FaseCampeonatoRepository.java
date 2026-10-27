package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.FaseCampeonatoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaseCampeonatoRepository extends JpaRepository<FaseCampeonatoEntity, Long> {

    List<FaseCampeonatoEntity> findByCampeonatoOrderByOrdem(CampeonatoEntity campeonato);

    Optional<FaseCampeonatoEntity> findByCampeonatoAndOrdem(CampeonatoEntity campeonato, int ordem);

    boolean existsByCampeonatoAndOrdem(CampeonatoEntity campeonato, int ordem);
}
