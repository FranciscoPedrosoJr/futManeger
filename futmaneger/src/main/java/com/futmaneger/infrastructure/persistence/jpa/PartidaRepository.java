package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TipoEventoCalendario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaRepository extends JpaRepository<PartidaEntity, Long> {

    List<PartidaEntity> findByEdicao(EdicaoCampeonatoEntity edicao);

    List<PartidaEntity> findByRodadaEntity(RodadaEntity rodadaEntity);

    boolean existsByRodadaEntityAndFinalizadaFalse(RodadaEntity rodadaEntity);

    boolean existsByEdicaoAndFinalizadaTrue(EdicaoCampeonatoEntity edicao);

    boolean existsByEdicao_Temporada_IdAndFinalizadaFalseAndDataProgramadaIsNull(Long temporadaId);

    void deleteByEdicao(EdicaoCampeonatoEntity edicao);

    @Query("SELECT p FROM PartidaEntity p WHERE p.edicao = :edicao AND p.rodada = :rodada")
    List<PartidaEntity> buscarPorEdicaoERodada(@Param("edicao") EdicaoCampeonatoEntity edicao,
                                               @Param("rodada") int rodada);

    @Query("""
            SELECT COUNT(p)
              FROM PartidaEntity p
             WHERE p.edicao.temporada.id = :temporadaId
               AND p.finalizada = false
               AND p.dataProgramada IS NOT NULL
               AND NOT EXISTS (
                    SELECT e.id
                      FROM EventoCalendarioEntity e
                     WHERE e.tipo = :tipo
                       AND e.partidaId = p.id
                       AND e.partidaMataMata = false
               )
            """)
    long countAgendadasSemEvento(@Param("temporadaId") Long temporadaId,
                                 @Param("tipo") TipoEventoCalendario tipo);
}
