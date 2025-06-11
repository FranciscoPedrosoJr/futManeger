package com.futmaneger.application.usecase.tecnico;


import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import com.futmaneger.infrastructure.persistence.jpa.TecnicoJpaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BuscarTecnicoUseCase {
    private final TecnicoJpaRepository tecnicoJpaRepository;

    public BuscarTecnicoUseCase(TecnicoJpaRepository tecnicoJpaRepository) {
        this.tecnicoJpaRepository = tecnicoJpaRepository;
    }

    public List<TecnicoEntity> buscarTodos() {
        return tecnicoJpaRepository.findAll();
    }

}
