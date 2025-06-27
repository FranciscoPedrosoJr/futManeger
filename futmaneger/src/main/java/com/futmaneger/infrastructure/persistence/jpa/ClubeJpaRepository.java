package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.domain.entity.Clube;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubeJpaRepository extends JpaRepository<Clube, Long> {
    List<Clube> findByEstado(String estado);

    List<Clube> findByPais(String pais);
}
