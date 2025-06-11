package com.futmaneger.application.usecase.clube;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BuscarClubesUseCase {
    private final ClubeJpaRepository clubeJpaRepository;

    public BuscarClubesUseCase(ClubeJpaRepository clubeJpaRepository) {
        this.clubeJpaRepository = clubeJpaRepository;
    }

    public List<Clube> buscarTodos() {
        return clubeJpaRepository.findAll();
    }
}
