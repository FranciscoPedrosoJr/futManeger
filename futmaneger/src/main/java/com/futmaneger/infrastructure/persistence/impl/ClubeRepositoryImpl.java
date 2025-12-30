package com.futmaneger.infrastructure.persistence.impl;

import com.futmaneger.domain.repository.ClubeRepository;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClubeRepositoryImpl implements ClubeRepository {

    private final ClubeJpaRepository clubeJpaRepository;

    @Override
    public Optional<ClubeEntity> buscarPorTecnicoId(Long tecnicoId) {
        return clubeJpaRepository.findByTecnico_Id(tecnicoId)
                .map(entity -> new ClubeEntity(
                        entity.getId(),
                        entity.getNome(),
                        entity.getEstado(),
                        entity.getPais(),
                        entity.getSaldo(),
                        entity.getTecnico().getId()
                ));
    }
}

