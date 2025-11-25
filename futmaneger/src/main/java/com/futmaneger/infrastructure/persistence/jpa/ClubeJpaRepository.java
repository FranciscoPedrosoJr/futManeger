package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClubeJpaRepository extends JpaRepository<ClubeEntity, Long> {
    List<ClubeEntity> findByEstado(String estado);

    List<ClubeEntity> findByPais(String pais);


    @Query("""
        SELECT AVG(j.forca)
        FROM Jogador j
        WHERE j.clube.id = :clubeId
    """)
    Double calcularMediaForca(Long clubeId);
}
