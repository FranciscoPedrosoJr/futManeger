package com.futmaneger.application.usecase.temporada;

import com.futmaneger.application.dto.CriarTemporadaRequestDTO;
import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.time.LocalDate;
import java.time.Year;
import org.springframework.stereotype.Service;

@Service
public class CriarTemporadaUseCase {

    private final TemporadaJpaRepository temporadaRepository;

    public CriarTemporadaUseCase(TemporadaJpaRepository temporadaRepository) {
        this.temporadaRepository = temporadaRepository;
    }

    public TemporadaResponseDTO executar(CriarTemporadaRequestDTO request) {
        int ano = (request != null && request.ano() != null)
                ? request.ano()
                : Year.now().getValue();

        if (temporadaRepository.existsByAno(ano)) {
            throw new DadosInvalidosException("Já existe uma temporada para o ano " + ano);
        }

        TemporadaEntity temporada = new TemporadaEntity();
        temporada.setAno(ano);
        temporada.setNome("Temporada " + ano);
        temporada.setEstado(TemporadaEntity.EstadoTemporada.AGUARDANDO_INICIO);
        temporada.setDataInicio(LocalDate.of(ano, 1, 1));
        temporada.setDataEncerramento(LocalDate.of(ano, 12, 31));

        TemporadaEntity salva = temporadaRepository.save(temporada);
        return TemporadaMapper.toResponse(salva);
    }
}
