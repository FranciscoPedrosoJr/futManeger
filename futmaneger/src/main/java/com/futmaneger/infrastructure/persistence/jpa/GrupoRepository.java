package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.GrupoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository extends JpaRepository<GrupoEntity, Long> {

    List<GrupoEntity> findByCampeonato(CampeonatoEntity campeonato);

    boolean existsByCampeonato(CampeonatoEntity campeonato);

    Optional<GrupoEntity> findByClubes_IdAndCampeonato_Id(Long clubeId, Long campeonatoId);
}
