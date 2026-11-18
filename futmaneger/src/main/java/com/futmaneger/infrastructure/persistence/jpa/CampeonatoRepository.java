package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.AbrangenciaCampeonato;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampeonatoRepository extends JpaRepository<CampeonatoEntity, Long> {
    List<CampeonatoEntity> findByTipoAndAbrangenciaIn(
            CampeonatoEntity.TipoCampeonato tipo, List<AbrangenciaCampeonato> abrangencias);
}
