package com.futmaneger.application.usecase.clube;

import com.futmaneger.application.dto.AtualizarClubeRequestDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TecnicoJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AtualizarClubeUseCase {
    private final ClubeJpaRepository clubeJpaRepository;
    private final TecnicoJpaRepository tecnicoJpaRepository;

    public ClubeEntity atualizar (Long clubeId, AtualizarClubeRequestDTO request){
        ClubeEntity clube = clubeJpaRepository.findById(clubeId)
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

      TecnicoEntity tecnico = tecnicoJpaRepository.findById(request.tecnicoId())
                .orElseThrow(() -> new NaoEncontradoException("Tecnico não encontrado"));

        if (request.nome() != null) {
            clube.setNome(request.nome());
        }

        if (request.estado() != null) {
            clube.setEstado(request.estado());
        }

        if (request.pais() != null) {
            clube.setPais(request.pais());
        }

        if(request.saldo() != null)
            clube.setSaldo(request.saldo());

        if (request.tecnicoId() != null) {
            clube.setTecnico(tecnico);
        }

        return clubeJpaRepository.save(clube);
    }

}
