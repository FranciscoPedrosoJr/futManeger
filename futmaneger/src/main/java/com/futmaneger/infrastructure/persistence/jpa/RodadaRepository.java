package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RodadaRepository extends JpaRepository<RodadaEntity, Long> {
    Optional<RodadaEntity> findByNumeroAndCampeonato(int rodada, CampeonatoEntity campeonato);

    @Query("SELECT MAX(r.numero) FROM RodadaEntity r WHERE r.campeonato = :campeonato")
    Optional<Integer> findMaxNumeroByCampeonato(@Param("campeonato") CampeonatoEntity campeonato);
}
