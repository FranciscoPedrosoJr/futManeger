package com.futmaneger.infrastructure.persistence.impl;

import com.futmaneger.domain.entity.Tecnico;
import com.futmaneger.domain.repository.TecnicoRepository;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import com.futmaneger.infrastructure.persistence.jpa.TecnicoJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TecnicoRepositoryImpl implements TecnicoRepository {

    private final TecnicoJpaRepository tecnicoJpaRepository;

    public TecnicoRepositoryImpl(TecnicoJpaRepository tecnicoJpaRepository) {
        this.tecnicoJpaRepository = tecnicoJpaRepository;
    }

    @Override
    public Tecnico salvar(Tecnico tecnico) {
        TecnicoEntity entity = new TecnicoEntity(
                tecnico.getId(),
                tecnico.getNome(),
                tecnico.getEmail(),
                tecnico.getSenha()
        );
        TecnicoEntity salvo = tecnicoJpaRepository.save(entity);
        return new Tecnico(
                salvo.getId(),
                salvo.getNome(),
                salvo.getEmail(),
                salvo.getSenha()
        );
    }

    @Override
    public Optional<Tecnico> buscarPorEmail(String email) {
        return tecnicoJpaRepository.findByEmail(email)
                .map(entity -> new Tecnico(
                        entity.getId(),
                        entity.getNome(),
                        entity.getEmail(),
                        entity.getSenha()
                ));
    }
}
