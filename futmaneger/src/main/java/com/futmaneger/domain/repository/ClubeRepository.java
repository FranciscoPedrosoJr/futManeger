package com.futmaneger.domain.repository;

import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import java.util.Optional;

public interface ClubeRepository {

    Optional<ClubeEntity> buscarPorTecnicoId(Long tecnicoId);

}

