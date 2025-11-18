package com.futmaneger.domain.repository;

import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface JogadorRepository {
    void saveAll(List<Jogador> jogadores);

    List<Jogador> findByClube(ClubeEntity clube);

    void save(Jogador jogador);

    Optional<Jogador> findById(Long id);
}
