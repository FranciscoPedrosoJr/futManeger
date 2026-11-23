package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TipoEventoCalendario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaMataMataRepository extends JpaRepository<PartidaMataMataEntity, Long> {

    boolean existsByEdicao(EdicaoCampeonatoEntity edicao);

    boolean existsByEdicaoAndFinalizadaTrue(EdicaoCampeonatoEntity edicao);

    boolean existsByEdicao_Temporada_IdAndFinalizadaFalseAndDataProgramadaIsNull(Long temporadaId);

    void deleteByEdicao(EdicaoCampeonatoEntity edicao);

    List<PartidaMataMataEntity> findByEdicaoId(Long edicaoId);

    List<PartidaMataMataEntity> findByRodada(RodadaEntity rodada);

    boolean existsByRodadaAndFinalizadaFalse(RodadaEntity rodada);

    boolean existsByEdicaoAndFase(EdicaoCampeonatoEntity edicao,
                                  PartidaMataMataEntity.FaseMataMata fase);

    @Query("SELECT p FROM PartidaMataMataEntity p WHERE p.rodada.id = :rodadaId")
    List<PartidaMataMataEntity> findByRodadaId(@Param("rodadaId") Long rodadaId);

    List<PartidaMataMataEntity> findByEdicaoAndOrdemFase(EdicaoCampeonatoEntity edicao,
                                                         Integer ordemFase);

    boolean existsByEdicaoAndOrdemFase(EdicaoCampeonatoEntity edicao, Integer ordemFase);

    @Query("""
            SELECT COUNT(p)
              FROM PartidaMataMataEntity p
             WHERE p.edicao.temporada.id = :temporadaId
               AND p.finalizada = false
               AND p.dataProgramada IS NOT NULL
               AND NOT EXISTS (
                    SELECT e.id
                      FROM EventoCalendarioEntity e
                     WHERE e.tipo = :tipo
                       AND e.partidaId = p.id
                       AND e.partidaMataMata = true
               )
            """)
    long countAgendadasSemEvento(@Param("temporadaId") Long temporadaId,
                                 @Param("tipo") TipoEventoCalendario tipo);
}
