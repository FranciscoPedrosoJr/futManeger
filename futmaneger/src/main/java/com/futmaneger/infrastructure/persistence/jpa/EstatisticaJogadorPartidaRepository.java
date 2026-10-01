package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EstatisticaJogadorPartidaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstatisticaJogadorPartidaRepository extends JpaRepository<EstatisticaJogadorPartidaEntity, Long> {
    List<EstatisticaJogadorPartidaEntity> findByPartidaIdAndPartidaMataMata(Long partidaId, boolean partidaMataMata);
}