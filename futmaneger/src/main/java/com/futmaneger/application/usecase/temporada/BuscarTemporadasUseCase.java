package com.futmaneger.application.usecase.temporada;

import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuscarTemporadasUseCase {

    private final TemporadaJpaRepository temporadaRepository;

    public BuscarTemporadasUseCase(TemporadaJpaRepository temporadaRepository) {
        this.temporadaRepository = temporadaRepository;
    }

    @Transactional(readOnly = true)
    public List<TemporadaResponseDTO> listar() {
        return temporadaRepository.findAll()
                .stream()
                .map(TemporadaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TemporadaResponseDTO buscarPorId(Long id) {
        return temporadaRepository.findById(id)
                .map(TemporadaMapper::toResponse)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));
    }
}
