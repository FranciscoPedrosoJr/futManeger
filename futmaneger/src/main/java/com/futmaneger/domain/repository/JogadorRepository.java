package com.futmaneger.domain.repository;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.domain.entity.Jogador;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface JogadorRepository {
    void saveAll(List<Jogador> jogadores);

    List<Jogador> findByClube(Clube clube);
}
