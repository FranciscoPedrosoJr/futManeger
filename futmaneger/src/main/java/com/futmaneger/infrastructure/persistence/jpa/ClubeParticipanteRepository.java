package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubeParticipanteRepository extends JpaRepository<ClubeParticipanteEntity, Long> {
    List<ClubeParticipanteEntity> findByCampeonato(CampeonatoEntity campeonato);
}
