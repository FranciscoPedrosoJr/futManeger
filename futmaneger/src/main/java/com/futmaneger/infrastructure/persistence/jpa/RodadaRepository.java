package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RodadaRepository extends JpaRepository<RodadaEntity, Long> {
}
