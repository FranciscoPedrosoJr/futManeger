package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EscalacaoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscalacaoRepository extends JpaRepository<EscalacaoEntity, Long> {
    Optional<EscalacaoEntity> findTopByClubeIdOrderByDataHoraDesc(Long clubeId);
}

