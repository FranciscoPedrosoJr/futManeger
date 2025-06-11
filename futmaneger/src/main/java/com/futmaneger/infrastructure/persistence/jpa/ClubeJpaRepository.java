package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.domain.entity.Clube;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubeJpaRepository extends JpaRepository<Clube, Long> {
}
