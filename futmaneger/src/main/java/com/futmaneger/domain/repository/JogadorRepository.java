package com.futmaneger.domain.repository;

import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface JogadorRepository {
    void saveAll(List<Jogador> jogadores);

    List<Jogador> findByClube(ClubeEntity clube);
}
