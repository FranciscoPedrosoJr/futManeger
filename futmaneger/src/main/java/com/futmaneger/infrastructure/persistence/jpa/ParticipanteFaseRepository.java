package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ParticipanteFaseEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipanteFaseRepository extends JpaRepository<ParticipanteFaseEntity, Long> {

    List<ParticipanteFaseEntity> findByEdicao(EdicaoCampeonatoEntity edicao);

    List<ParticipanteFaseEntity> findByEdicaoAndOrdemEntrada(EdicaoCampeonatoEntity edicao, int ordemEntrada);
}
