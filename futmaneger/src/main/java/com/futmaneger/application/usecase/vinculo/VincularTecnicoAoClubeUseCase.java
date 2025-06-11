package com.futmaneger.application.usecase.vinculo;

import com.futmaneger.application.dto.VinculoTecnicoClubeRequestDTO;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.domain.entity.Tecnico;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TecnicoJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class VincularTecnicoAoClubeUseCase {

    private final ClubeJpaRepository clubeRepository;
    private final TecnicoJpaRepository tecnicoRepository;

    public VincularTecnicoAoClubeUseCase(ClubeJpaRepository clubeRepository, TecnicoJpaRepository tecnicoRepository) {
        this.clubeRepository = clubeRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    public void vincular(VinculoTecnicoClubeRequestDTO request) {
        Clube clube = clubeRepository.findById(request.clubeId())
                .orElseThrow(() -> new RuntimeException("Clube não encontrado"));
        TecnicoEntity tecnico = tecnicoRepository.findById(request.tecnicoId())
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));

        clube.setTecnico(tecnico);
        clubeRepository.save(clube);
    }
}
