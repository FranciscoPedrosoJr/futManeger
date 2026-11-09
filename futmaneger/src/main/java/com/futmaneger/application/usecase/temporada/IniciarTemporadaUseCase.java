package com.futmaneger.application.usecase.temporada;

import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.calendario.AgendamentoCalendarioService;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prepara e inicia a temporada: gera o calendário global (que valida se as
 * partidas podem ser agendadas) e marca a temporada como em andamento. Após o
 * início, o calendário é considerado oficial.
 */
@Service
public class IniciarTemporadaUseCase {

    private final TemporadaJpaRepository temporadaRepository;
    private final AgendamentoCalendarioService agendamentoCalendario;

    public IniciarTemporadaUseCase(TemporadaJpaRepository temporadaRepository,
                                   AgendamentoCalendarioService agendamentoCalendario) {
        this.temporadaRepository = temporadaRepository;
        this.agendamentoCalendario = agendamentoCalendario;
    }

    @Transactional
    public TemporadaResponseDTO executar(Long temporadaId) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));

        if (temporada.getEstado() != TemporadaEntity.EstadoTemporada.AGUARDANDO_INICIO) {
            throw new DadosInvalidosException("A temporada só pode ser iniciada quando está aguardando início");
        }

        if (temporada.getEdicoes().isEmpty()) {
            throw new DadosInvalidosException("A temporada não possui campeonatos para iniciar");
        }

        agendamentoCalendario.gerarCalendario(temporada);

        temporada.setCalendarioGerado(true);
        temporada.iniciar();

        TemporadaEntity salva = temporadaRepository.save(temporada);
        return TemporadaMapper.toResponse(salva);
    }
}
