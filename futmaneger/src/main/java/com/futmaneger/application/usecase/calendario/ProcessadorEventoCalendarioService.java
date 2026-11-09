package com.futmaneger.application.usecase.calendario;

import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.simulacao.SimularRodadaUseCase;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Processa um acontecimento do calendário por vez. Cada evento é tratado em sua
 * própria transação: ou a partida é simulada e o evento concluído, ou nada é
 * alterado e o erro é registrado no evento para permitir a retomada.
 */
@Service
public class ProcessadorEventoCalendarioService {

    private final EventoCalendarioRepository eventoRepository;
    private final TemporadaJpaRepository temporadaRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final SimularRodadaUseCase simularRodadaUseCase;

    public ProcessadorEventoCalendarioService(EventoCalendarioRepository eventoRepository,
                                              TemporadaJpaRepository temporadaRepository,
                                              PartidaRepository partidaRepository,
                                              PartidaMataMataRepository partidaMataMataRepository,
                                              SimularRodadaUseCase simularRodadaUseCase) {
        this.eventoRepository = eventoRepository;
        this.temporadaRepository = temporadaRepository;
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.simularRodadaUseCase = simularRodadaUseCase;
    }

    /**
     * Simula a partida do evento (se ainda não concluída) e o marca como concluído.
     *
     * @return o resultado detalhado da simulação, ou null se o evento já estava
     *         concluído ou a partida já havia sido disputada por outro caminho.
     */
    @Transactional
    public SimulacaoResponseDTO processarPartida(Long eventoId, boolean escalacaoEstrita) {
        EventoCalendarioEntity evento = buscar(eventoId);
        if (!evento.estaAberto()) {
            return null;
        }

        TemporadaEntity temporada = evento.getTemporada();
        temporada.avancarDataAtualPara(evento.getData());
        temporadaRepository.save(temporada);

        SimulacaoResponseDTO resultado = null;
        if (!partidaJaFinalizada(evento)) {
            resultado = simularRodadaUseCase.simularPartidaAgendada(
                    evento.getEdicao().getId(), evento.getPartidaId(), evento.isPartidaMataMata(),
                    escalacaoEstrita, evento.getData());
        }

        evento.concluir();
        eventoRepository.save(evento);
        return resultado;
    }

    /**
     * Leva a data atual da temporada até o dia do acontecimento, sem passar dele.
     */
    @Transactional
    public void registrarChegadaNaData(Long temporadaId, LocalDate data) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));
        temporada.avancarDataAtualPara(data);
        temporadaRepository.save(temporada);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErro(Long eventoId, String mensagem) {
        EventoCalendarioEntity evento = buscar(eventoId);
        evento.registrarErro(mensagem);
        eventoRepository.save(evento);
    }

    @Transactional(readOnly = true)
    public boolean envolveClube(Long eventoId, Long clubeId) {
        EventoCalendarioEntity evento = buscar(eventoId);
        if (evento.isPartidaMataMata()) {
            return partidaMataMataRepository.findById(evento.getPartidaId())
                    .map(p -> clubeId.equals(p.getMandante().getId()) || clubeId.equals(p.getVisitante().getId()))
                    .orElse(false);
        }
        return partidaRepository.findById(evento.getPartidaId())
                .map(p -> clubeId.equals(p.getMandante().getId()) || clubeId.equals(p.getVisitante().getId()))
                .orElse(false);
    }

    private boolean partidaJaFinalizada(EventoCalendarioEntity evento) {
        if (evento.isPartidaMataMata()) {
            return partidaMataMataRepository.findById(evento.getPartidaId())
                    .map(PartidaMataMataEntity::isFinalizada)
                    .orElseThrow(() -> new NaoEncontradoException("Partida do evento não encontrada"));
        }
        return partidaRepository.findById(evento.getPartidaId())
                .map(PartidaEntity::isFinalizada)
                .orElseThrow(() -> new NaoEncontradoException("Partida do evento não encontrada"));
    }

    private EventoCalendarioEntity buscar(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new NaoEncontradoException("Acontecimento do calendário não encontrado"));
    }
}
