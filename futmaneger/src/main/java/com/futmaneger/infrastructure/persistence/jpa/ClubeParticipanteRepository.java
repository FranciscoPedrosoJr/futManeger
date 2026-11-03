package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubeParticipanteRepository extends JpaRepository<ClubeParticipanteEntity, Long> {
    List<ClubeParticipanteEntity> findByEdicao(EdicaoCampeonatoEntity edicao);

    Optional<ClubeParticipanteEntity> findByClubeAndEdicao(ClubeEntity clube, EdicaoCampeonatoEntity edicao);
}
