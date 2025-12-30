package com.futmaneger.domain.repository;

import com.futmaneger.domain.entity.Tecnico;
import java.util.Optional;

public interface TecnicoRepository {
    Tecnico salvar(Tecnico tecnico);

    Optional<Tecnico> buscarPorEmail(String email);

    Optional<Tecnico> buscaPorId(Long tecnicoId);
}