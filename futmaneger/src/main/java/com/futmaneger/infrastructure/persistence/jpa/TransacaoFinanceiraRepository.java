package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.TransacaoFinanceiraEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoFinanceiraRepository extends JpaRepository<TransacaoFinanceiraEntity, UUID> {

    List<TransacaoFinanceiraEntity> findByClubeIdOrderByDataDesc(Long clubeId);
}
