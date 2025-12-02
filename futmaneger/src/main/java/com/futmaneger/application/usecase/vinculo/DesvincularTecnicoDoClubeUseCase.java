package com.futmaneger.application.usecase.vinculo;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DesvincularTecnicoDoClubeUseCase {

    private final ClubeJpaRepository clubeRepository;

    public DesvincularTecnicoDoClubeUseCase(ClubeJpaRepository clubeRepository) {
        this.clubeRepository = clubeRepository;
    }

    public void desvincular(Long clubeId, Long tecnicoId) {

        ClubeEntity clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

        if (clube.getTecnico() == null) {
            throw new DadosInvalidosException("Esse clube não possui técnico vinculado");
        }

        if (!clube.getTecnico().getId().equals(tecnicoId)) {
            throw new DadosInvalidosException("Este técnico não está vinculado a este clube");
        }

        clube.setTecnico(null);

        clubeRepository.save(clube);
    }
}

