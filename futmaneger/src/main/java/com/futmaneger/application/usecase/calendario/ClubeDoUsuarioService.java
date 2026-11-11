package com.futmaneger.application.usecase.calendario;

import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TecnicoJpaRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Identifica o clube vinculado ao técnico autenticado na requisição atual.
 */
@Service
public class ClubeDoUsuarioService {

    private final TecnicoJpaRepository tecnicoRepository;
    private final ClubeJpaRepository clubeRepository;

    public ClubeDoUsuarioService(TecnicoJpaRepository tecnicoRepository, ClubeJpaRepository clubeRepository) {
        this.tecnicoRepository = tecnicoRepository;
        this.clubeRepository = clubeRepository;
    }

    public Optional<ClubeEntity> obterClubeDoUsuarioAutenticado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UserDetails usuario)) {
            return Optional.empty();
        }
        return tecnicoRepository.findByEmail(usuario.getUsername())
                .flatMap(tecnico -> clubeRepository.findByTecnico_Id(tecnico.getId()));
    }
}
