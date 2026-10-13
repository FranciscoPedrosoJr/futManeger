package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.GrupoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository extends JpaRepository<GrupoEntity, Long> {

    List<GrupoEntity> findByEdicao(EdicaoCampeonatoEntity edicao);

    boolean existsByEdicao(EdicaoCampeonatoEntity edicao);

    Optional<GrupoEntity> findByClubes_IdAndEdicao_Id(Long clubeId, Long edicaoId);
}
