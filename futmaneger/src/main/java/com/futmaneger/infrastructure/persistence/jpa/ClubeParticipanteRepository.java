package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubeParticipanteRepository extends JpaRepository<ClubeParticipanteEntity, Long> {
}
