package com.futmaneger.application.usecase.clube;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ExcluirClubeUseCase {
    private final ClubeJpaRepository clubeJpaRepository;

    public ExcluirClubeUseCase(ClubeJpaRepository clubeJpaRepository) {
        this.clubeJpaRepository = clubeJpaRepository;
    }

    public Optional<String> excluirPorId(Long id) {
        Optional<ClubeEntity> clubeOptional = clubeJpaRepository.findById(id);
        if (clubeOptional.isPresent()) {
            clubeJpaRepository.deleteById(id);
            return Optional.of(clubeOptional.get().getNome());
        }
        return Optional.empty();
    }
}
