package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.entity.StatusEventoCalendario;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TipoEventoCalendario;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoCalendarioRepository extends JpaRepository<EventoCalendarioEntity, Long> {

    List<EventoCalendarioEntity> findByTemporadaOrderByDataAscOrdemAscIdAsc(TemporadaEntity temporada);

    @EntityGraph(attributePaths = {"edicao", "edicao.campeonato"})
    Optional<EventoCalendarioEntity> findFirstByTemporadaAndStatusInOrderByDataAscOrdemAscIdAsc(
            TemporadaEntity temporada, List<StatusEventoCalendario> status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"temporada", "edicao", "edicao.campeonato"})
    @Query("SELECT e FROM EventoCalendarioEntity e WHERE e.id = :id")
    Optional<EventoCalendarioEntity> findByIdForUpdate(@Param("id") Long id);

    boolean existsByTipoAndPartidaIdAndPartidaMataMata(TipoEventoCalendario tipo, Long partidaId,
                                                       boolean partidaMataMata);

    long countByEdicao(EdicaoCampeonatoEntity edicao);

    long countByTemporadaAndObrigatorioTrueAndStatusIn(TemporadaEntity temporada,
                                                       List<StatusEventoCalendario> status);

    boolean existsByTemporadaIdAndObrigatorioTrueAndStatusIn(
            Long temporadaId, List<StatusEventoCalendario> status);

    @Query("""
            SELECT e.partidaId AS partidaId,
                   e.partidaMataMata AS partidaMataMata
              FROM EventoCalendarioEntity e
             WHERE e.temporada.id = :temporadaId
               AND e.tipo = :tipo
               AND e.partidaId IS NOT NULL
            """)
    List<ReferenciaPartidaEvento> findReferenciasPartidas(
            @Param("temporadaId") Long temporadaId,
            @Param("tipo") TipoEventoCalendario tipo);

    interface ReferenciaPartidaEvento {
        Long getPartidaId();

        Boolean getPartidaMataMata();
    }
}
