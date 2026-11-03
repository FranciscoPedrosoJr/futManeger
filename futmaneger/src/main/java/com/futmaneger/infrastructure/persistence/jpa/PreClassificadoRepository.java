package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PreClassificadoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreClassificadoRepository extends JpaRepository<PreClassificadoEntity, Long> {

    List<PreClassificadoEntity> findByCampeonatoDestinoAndConsumidoFalse(CampeonatoEntity campeonatoDestino);
}
