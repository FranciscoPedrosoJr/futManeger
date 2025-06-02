package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.domain.entity.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JogadorJpaRepository extends JpaRepository<Jogador, Long> , JpaSpecificationExecutor<Jogador> {}