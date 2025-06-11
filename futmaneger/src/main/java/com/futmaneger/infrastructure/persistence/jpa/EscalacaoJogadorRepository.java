package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EscalacaoEntity;
import com.futmaneger.infrastructure.persistence.entity.EscalacaoJogadorEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscalacaoJogadorRepository extends JpaRepository<EscalacaoJogadorEntity, Long> {
    List<EscalacaoJogadorEntity> findByEscalacao(EscalacaoEntity escalacao);
}

