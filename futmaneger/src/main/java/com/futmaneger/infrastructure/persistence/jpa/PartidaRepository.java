package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaFaseDeGruposEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaRepository extends JpaRepository<PartidaEntity, Long> {
    List<PartidaFaseDeGruposEntity> findByCampeonato(CampeonatoEntity campeonato);


    @Query("SELECT p FROM PartidaEntity p WHERE p.campeonato = :campeonato AND p.rodada = :rodada")
    List<PartidaEntity> buscarPorCampeonatoERodada(@Param("campeonato") CampeonatoEntity campeonato, @Param("rodada") int rodada);

}
