package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TecnicoJpaRepository extends JpaRepository<TecnicoEntity, Long> {

    Optional<TecnicoEntity> findByEmail(String email);
}
