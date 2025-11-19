package com.futmaneger.application.usecase.vinculo;

import com.futmaneger.application.dto.VinculoTecnicoClubeRequestDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.domain.entity.Tecnico;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TecnicoJpaRepository;
import java.math.BigDecimal;
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
        ClubeEntity clube = clubeRepository.findById(request.clubeId())
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));
        TecnicoEntity tecnico = tecnicoRepository.findById(request.tecnicoId())
                .orElseThrow(() -> new NaoEncontradoException("Técnico não encontrado"));

        clube.setTecnico(tecnico);
        clube.setSaldo(BigDecimal.valueOf(Long.parseLong("1")));
        clubeRepository.save(clube);
    }
}
