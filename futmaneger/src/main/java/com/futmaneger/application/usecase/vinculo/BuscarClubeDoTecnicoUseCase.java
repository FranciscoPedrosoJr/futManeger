package com.futmaneger.application.usecase.vinculo;

import com.futmaneger.application.dto.TecnicoClubeResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.domain.entity.Tecnico;
import com.futmaneger.domain.repository.ClubeRepository;
import com.futmaneger.domain.repository.TecnicoRepository;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarClubeDoTecnicoUseCase {

    private final ClubeRepository clubeRepository;

    public TecnicoClubeResponseDTO executar(Long tecnicoId) {

        return clubeRepository.buscarPorTecnicoId(tecnicoId)
                .map(clube -> new TecnicoClubeResponseDTO(
                        clube.getId(),
                        clube.getNome()
                ))
                .orElse(new TecnicoClubeResponseDTO(null, null));
    }
}
