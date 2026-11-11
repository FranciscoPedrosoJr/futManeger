package com.futmaneger.application.usecase.calendario;

import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.entity.StatusEventoCalendario;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import org.springframework.stereotype.Service;

/**
 * Disputa a partida do clube do usuário que interrompeu o avanço. Só é possível
 * simular o próximo acontecimento pendente, e apenas se ele envolver o clube do
 * técnico autenticado.
 */
@Service
public class SimularPartidaDoUsuarioUseCase {

    private final TemporadaJpaRepository temporadaRepository;
    private final EventoCalendarioRepository eventoRepository;
    private final ProcessadorEventoCalendarioService processador;
    private final AgendamentoCalendarioService agendamentoCalendario;
    private final ClubeDoUsuarioService clubeDoUsuarioService;

    public SimularPartidaDoUsuarioUseCase(TemporadaJpaRepository temporadaRepository,
                                          EventoCalendarioRepository eventoRepository,
                                          ProcessadorEventoCalendarioService processador,
                                          AgendamentoCalendarioService agendamentoCalendario,
                                          ClubeDoUsuarioService clubeDoUsuarioService) {
        this.temporadaRepository = temporadaRepository;
        this.eventoRepository = eventoRepository;
        this.processador = processador;
        this.agendamentoCalendario = agendamentoCalendario;
        this.clubeDoUsuarioService = clubeDoUsuarioService;
    }

    public SimulacaoResponseDTO executar(Long temporadaId, Long eventoId) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));

        if (temporada.getEstado() != TemporadaEntity.EstadoTemporada.EM_ANDAMENTO) {
            throw new DadosInvalidosException("Somente temporadas em andamento permitem disputar partidas");
        }

        ClubeEntity clubeUsuario = clubeDoUsuarioService.obterClubeDoUsuarioAutenticado()
                .orElseThrow(() -> new DadosInvalidosException(
                        "O técnico autenticado não está vinculado a nenhum clube"));

        EventoCalendarioEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new NaoEncontradoException("Acontecimento do calendário não encontrado"));

        if (!evento.getTemporada().getId().equals(temporadaId)) {
            throw new DadosInvalidosException("O acontecimento informado não pertence a esta temporada");
        }
        if (!evento.estaAberto()) {
            throw new DadosInvalidosException("Este acontecimento já foi concluído");
        }

        EventoCalendarioEntity proximo = eventoRepository
                .findFirstByTemporadaAndStatusInOrderByDataAscOrdemAscIdAsc(
                        temporada, StatusEventoCalendario.abertos())
                .orElseThrow(() -> new DadosInvalidosException("Não há acontecimentos pendentes"));

        if (!proximo.getId().equals(evento.getId())) {
            throw new DadosInvalidosException(
                    "Existem acontecimentos anteriores pendentes. Avance a temporada antes de disputar esta partida");
        }

        if (!processador.envolveClube(eventoId, clubeUsuario.getId())) {
            throw new DadosInvalidosException(
                    "Esta partida não envolve o seu clube; partidas de outros clubes são simuladas pelo avanço");
        }

        SimulacaoResponseDTO resultado;
        try {
            resultado = processador.processarPartida(eventoId, true);
        } catch (RuntimeException e) {
            processador.registrarErro(eventoId, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            throw e;
        }

        if (resultado == null) {
            throw new DadosInvalidosException(
                    "A partida já havia sido concluída; o calendário foi sincronizado");
        }

        try {
            agendamentoCalendario.sincronizarPorId(temporadaId);
        } catch (RuntimeException ignorada) {
            // A partida já foi registrada. Falhas de agendamento das próximas partidas
            // são reportadas no próximo avanço da temporada.
        }

        return resultado;
    }
}
