package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporadaJpaRepository extends JpaRepository<TemporadaEntity, Long> {

    Optional<TemporadaEntity> findByAno(Integer ano);

    boolean existsByAno(Integer ano);
}
