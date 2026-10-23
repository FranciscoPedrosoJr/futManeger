package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaRepository extends JpaRepository<PartidaEntity, Long> {

    List<PartidaEntity> findByEdicao(EdicaoCampeonatoEntity edicao);

    @Query("SELECT p FROM PartidaEntity p WHERE p.edicao = :edicao AND p.rodada = :rodada")
    List<PartidaEntity> buscarPorEdicaoERodada(@Param("edicao") EdicaoCampeonatoEntity edicao, @Param("rodada") int rodada);

}
