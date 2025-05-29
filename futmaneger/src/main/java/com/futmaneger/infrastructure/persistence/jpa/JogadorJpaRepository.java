package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.domain.entity.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JogadorJpaRepository extends JpaRepository<Jogador, Long> {
}
