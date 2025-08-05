package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JogadorJpaRepository extends JpaRepository<Jogador, Long> , JpaSpecificationExecutor<Jogador> {
    List<Jogador> findByClube(ClubeEntity clube);
}