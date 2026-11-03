package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.PaisEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaisRepository extends JpaRepository<PaisEntity, Long> {

    Optional<PaisEntity> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
