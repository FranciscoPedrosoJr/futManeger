package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.entity.StatusEventoCalendario;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TipoEventoCalendario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoCalendarioRepository extends JpaRepository<EventoCalendarioEntity, Long> {

    List<EventoCalendarioEntity> findByTemporadaOrderByDataAscOrdemAscIdAsc(TemporadaEntity temporada);

    Optional<EventoCalendarioEntity> findFirstByTemporadaAndStatusInOrderByDataAscOrdemAscIdAsc(
            TemporadaEntity temporada, List<StatusEventoCalendario> status);

    boolean existsByTipoAndPartidaIdAndPartidaMataMata(TipoEventoCalendario tipo, Long partidaId,
                                                       boolean partidaMataMata);

    long countByEdicao(EdicaoCampeonatoEntity edicao);

    long countByTemporadaAndObrigatorioTrueAndStatusIn(TemporadaEntity temporada,
                                                       List<StatusEventoCalendario> status);
}
