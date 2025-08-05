package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.GrupoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository extends JpaRepository<GrupoEntity, Long> {

    List<GrupoEntity> findByCampeonato(CampeonatoEntity campeonato);

    boolean existsByCampeonato(CampeonatoEntity campeonato);
}
