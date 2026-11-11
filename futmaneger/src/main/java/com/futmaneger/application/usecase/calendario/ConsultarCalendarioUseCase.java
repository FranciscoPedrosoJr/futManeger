package com.futmaneger.application.usecase.calendario;

import com.futmaneger.application.dto.EventoCalendarioResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.StatusEventoCalendario;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarCalendarioUseCase {

    private final TemporadaJpaRepository temporadaRepository;
    private final EventoCalendarioRepository eventoRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final ClubeDoUsuarioService clubeDoUsuarioService;

    public ConsultarCalendarioUseCase(TemporadaJpaRepository temporadaRepository,
                                      EventoCalendarioRepository eventoRepository,
                                      PartidaRepository partidaRepository,
                                      PartidaMataMataRepository partidaMataMataRepository,
                                      ClubeDoUsuarioService clubeDoUsuarioService) {
        this.temporadaRepository = temporadaRepository;
        this.eventoRepository = eventoRepository;
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.clubeDoUsuarioService = clubeDoUsuarioService;
    }

    @Transactional(readOnly = true)
    public List<EventoCalendarioResponseDTO> listar(Long temporadaId, Long clubeId) {
        TemporadaEntity temporada = buscarTemporada(temporadaId);
        List<EventoCalendarioResponseDTO> eventos =
                montar(eventoRepository.findByTemporadaOrderByDataAscOrdemAscIdAsc(temporada));

        if (clubeId == null) {
            return eventos;
        }
        return eventos.stream()
                .filter(e -> clubeId.equals(e.mandanteId()) || clubeId.equals(e.visitanteId()))
                .toList();
    }

    /**
     * Próximo acontecimento ainda não concluído, ou null quando não há pendências.
     */
    @Transactional(readOnly = true)
    public EventoCalendarioResponseDTO proximo(Long temporadaId) {
        TemporadaEntity temporada = buscarTemporada(temporadaId);
        return eventoRepository
                .findFirstByTemporadaAndStatusInOrderByDataAscOrdemAscIdAsc(temporada, StatusEventoCalendario.abertos())
                .map(evento -> montar(List.of(evento)).get(0))
                .orElse(null);
    }

    /**
     * Indica se não restam acontecimentos obrigatórios nem campeonatos em andamento.
     */
    @Transactional(readOnly = true)
    public boolean podeFinalizar(Long temporadaId) {
        TemporadaEntity temporada = buscarTemporada(temporadaId);
        long pendentes = eventoRepository.countByTemporadaAndObrigatorioTrueAndStatusIn(
                temporada, StatusEventoCalendario.abertos());
        boolean campeonatosConcluidos = temporada.getEdicoes().stream()
                .noneMatch(EdicaoCampeonatoEntity::getEmAndamento);
        return pendentes == 0 && campeonatosConcluidos;
    }

    private TemporadaEntity buscarTemporada(Long temporadaId) {
        return temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));
    }

    private List<EventoCalendarioResponseDTO> montar(List<EventoCalendarioEntity> eventos) {
        Long clubeUsuarioId = clubeDoUsuarioService.obterClubeDoUsuarioAutenticado()
                .map(ClubeEntity::getId)
                .orElse(null);

        List<Long> idsNormais = idsDePartidas(eventos, false);
        List<Long> idsEliminatorias = idsDePartidas(eventos, true);

        Map<Long, PartidaEntity> normais = partidaRepository.findAllById(idsNormais).stream()
                .collect(Collectors.toMap(PartidaEntity::getId, Function.identity()));
        Map<Long, PartidaMataMataEntity> eliminatorias = partidaMataMataRepository.findAllById(idsEliminatorias)
                .stream()
                .collect(Collectors.toMap(PartidaMataMataEntity::getId, Function.identity()));

        return eventos.stream()
                .map(evento -> evento.isPartidaMataMata()
                        ? paraDto(evento, eliminatorias.get(evento.getPartidaId()), clubeUsuarioId)
                        : paraDto(evento, normais.get(evento.getPartidaId()), clubeUsuarioId))
                .toList();
    }

    private List<Long> idsDePartidas(List<EventoCalendarioEntity> eventos, boolean mataMata) {
        return eventos.stream()
                .filter(e -> e.isPartidaMataMata() == mataMata)
                .map(EventoCalendarioEntity::getPartidaId)
                .filter(Objects::nonNull)
                .toList();
    }

    private EventoCalendarioResponseDTO paraDto(EventoCalendarioEntity evento, PartidaEntity partida,
                                                Long clubeUsuarioId) {
        if (partida == null) {
            return semPartida(evento);
        }
        return construir(evento, partida.getRodada(), partida.getMandante(), partida.getVisitante(),
                partida.isFinalizada() ? partida.getGolsMandante() : null,
                partida.isFinalizada() ? partida.getGolsVisitante() : null,
                clubeUsuarioId);
    }

    private EventoCalendarioResponseDTO paraDto(EventoCalendarioEntity evento, PartidaMataMataEntity partida,
                                                Long clubeUsuarioId) {
        if (partida == null) {
            return semPartida(evento);
        }
        Integer rodada = partida.getRodada() != null ? partida.getRodada().getNumero() : null;
        return construir(evento, rodada, partida.getMandante(), partida.getVisitante(),
                partida.isFinalizada() ? partida.getGolsClubeA() : null,
                partida.isFinalizada() ? partida.getGolsClubeB() : null,
                clubeUsuarioId);
    }

    private EventoCalendarioResponseDTO construir(EventoCalendarioEntity evento, Integer rodada,
                                                  ClubeEntity mandante, ClubeEntity visitante,
                                                  Integer golsMandante, Integer golsVisitante,
                                                  Long clubeUsuarioId) {
        boolean envolveUsuario = clubeUsuarioId != null
                && (clubeUsuarioId.equals(mandante.getId()) || clubeUsuarioId.equals(visitante.getId()));

        return new EventoCalendarioResponseDTO(
                evento.getId(),
                evento.getTipo().name(),
                evento.getData(),
                evento.getStatus().name(),
                evento.isObrigatorio(),
                evento.getEdicao() != null ? evento.getEdicao().getId() : null,
                evento.getEdicao() != null ? evento.getEdicao().getCampeonato().getId() : null,
                evento.getEdicao() != null ? evento.getEdicao().getCampeonato().getNome() : null,
                rodada,
                evento.getPartidaId(),
                evento.isPartidaMataMata(),
                mandante.getId(),
                mandante.getNome(),
                visitante.getId(),
                visitante.getNome(),
                golsMandante,
                golsVisitante,
                envolveUsuario,
                evento.getMensagemErro()
        );
    }

    private EventoCalendarioResponseDTO semPartida(EventoCalendarioEntity evento) {
        return new EventoCalendarioResponseDTO(
                evento.getId(), evento.getTipo().name(), evento.getData(), evento.getStatus().name(),
                evento.isObrigatorio(),
                evento.getEdicao() != null ? evento.getEdicao().getId() : null,
                evento.getEdicao() != null ? evento.getEdicao().getCampeonato().getId() : null,
                evento.getEdicao() != null ? evento.getEdicao().getCampeonato().getNome() : null,
                null, evento.getPartidaId(), evento.isPartidaMataMata(),
                null, null, null, null, null, null, false, evento.getMensagemErro());
    }
}
