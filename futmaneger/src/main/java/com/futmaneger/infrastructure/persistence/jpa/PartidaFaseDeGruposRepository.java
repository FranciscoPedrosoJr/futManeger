package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaFaseDeGruposEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaFaseDeGruposRepository extends JpaRepository<PartidaFaseDeGruposEntity, Long> {
    List<PartidaFaseDeGruposEntity> findByEdicao(EdicaoCampeonatoEntity edicao);
}
