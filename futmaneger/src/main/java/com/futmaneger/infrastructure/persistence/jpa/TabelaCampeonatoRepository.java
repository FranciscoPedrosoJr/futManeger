package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TabelaCampeonatoRepository extends JpaRepository<TabelaCampeonatoEntity, Long> {
    Optional<TabelaCampeonatoEntity> findByCampeonatoAndClube(CampeonatoEntity campeonato, Clube clube);
}
