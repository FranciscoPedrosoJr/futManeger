package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.HistoricoTransferenciaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoTransferenciaRepository
        extends JpaRepository<HistoricoTransferenciaEntity, UUID> {
}
