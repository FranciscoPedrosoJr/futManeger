package com.futmaneger.application.usecase.tecnico;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TecnicoJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ExcluirTecnicoUseCase {
    private final TecnicoJpaRepository tecnicoJpaRepository;

    public ExcluirTecnicoUseCase(TecnicoJpaRepository tecnicoJpaRepository) {
        this.tecnicoJpaRepository = tecnicoJpaRepository;
    }

    public Optional<String> excluirPorId(Long id) {
        Optional<TecnicoEntity> tecnicoOptional = tecnicoJpaRepository.findById(id);
        if (tecnicoOptional.isPresent()) {
            tecnicoJpaRepository.deleteById(id);
            return Optional.of(tecnicoOptional.get().getNome());
        }
        return Optional.empty();
    }
}
