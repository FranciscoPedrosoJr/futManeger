package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RodadaRepository extends JpaRepository<RodadaEntity, Long> {
    Optional<RodadaEntity> findByNumeroAndEdicao(int rodada, EdicaoCampeonatoEntity edicao);

    @Query("SELECT MAX(r.numero) FROM RodadaEntity r WHERE r.edicao = :edicao")
    Optional<Integer> findMaxNumeroByEdicao(@Param("edicao") EdicaoCampeonatoEntity edicao);

    @Query("""

        SELECT r
        FROM RodadaEntity r
        JOIN r.edicao e
        WHERE r.numero = :rodadaId AND e.id = :edicaoId

    """)
    Optional<RodadaEntity> findByIdAndEdicaoIdWithFetch(
            @Param("rodadaId") Long rodadaId,
            @Param("edicaoId") Long edicaoId
    );

    List<RodadaEntity> findByFinalizadaFalse();

    List<RodadaEntity> findByEdicaoIdAndFinalizadaFalseOrderByNumero(Long edicaoId);
}
