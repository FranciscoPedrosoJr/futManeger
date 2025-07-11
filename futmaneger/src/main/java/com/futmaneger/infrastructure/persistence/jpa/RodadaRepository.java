package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RodadaRepository extends JpaRepository<RodadaEntity, Long> {
}
