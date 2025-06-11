package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.domain.entity.Jogador;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JogadorJpaRepository extends JpaRepository<Jogador, Long> , JpaSpecificationExecutor<Jogador> {
    List<Jogador> findByClube(Clube clube);
}