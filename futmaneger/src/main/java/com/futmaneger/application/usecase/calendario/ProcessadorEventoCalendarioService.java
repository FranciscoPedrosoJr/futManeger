package com.futmaneger.application.usecase.calendario;

import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.simulacao.ResultadoSimulacaoAgendada;
import com.futmaneger.application.usecase.simulacao.SimularRodadaUseCase;
import com.futmaneger.domain.entity.PartidaSimulavel;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Processa um evento por transação. O bloqueio pessimista impede que duas
 * chamadas apliquem resultado, estatísticas e efeitos financeiros em duplicidade.
 */
@Service
public class ProcessadorEventoCalendarioService {

    private final EventoCalendarioRepository eventoRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final SimularRodadaUseCase simularRodadaUseCase;

    public ProcessadorEventoCalendarioService(EventoCalendarioRepository eventoRepository,
                                              PartidaRepository partidaRepository,
                                              PartidaMataMataRepository partidaMataMataRepository,
                                              SimularRodadaUseCase simularRodadaUseCase) {
        this.eventoRepository = eventoRepository;
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.simularRodadaUseCase = simularRodadaUseCase;
    }

    @Transactional
    public ResultadoProcessamentoEvento processarParaAvanco(Long eventoId, Long clubeUsuarioId) {
        return processar(eventoId, clubeUsuarioId, ModoProcessamento.AVANCO_AUTOMATICO);
    }

    @Transactional
    public ResultadoProcessamentoEvento processarPartidaDoUsuario(Long eventoId, Long clubeUsuarioId) {
        return processar(eventoId, clubeUsuarioId, ModoProcessamento.PARTIDA_DO_USUARIO);
    }

    private ResultadoProcessamentoEvento processar(Long eventoId, Long clubeUsuarioId,
                                                    ModoProcessamento modo) {
        EventoCalendarioEntity evento = eventoRepository.findByIdForUpdate(eventoId)
                .orElseThrow(() -> new NaoEncontradoException(
                        "Acontecimento do calendário não encontrado"));
        if (!evento.estaAberto()) {
            return ResultadoProcessamentoEvento.paraIgnorado();
        }

        PartidaSimulavel partida = carregarPartida(evento);
        boolean envolveUsuario = clubeUsuarioId.equals(partida.getMandante().getId())
                || clubeUsuarioId.equals(partida.getVisitante().getId());

        if (modo == ModoProcessamento.AVANCO_AUTOMATICO && envolveUsuario) {
            evento.getTemporada().avancarDataAtualPara(evento.getData());
            return ResultadoProcessamentoEvento.paraAguardarUsuario();
        }
        if (modo == ModoProcessamento.PARTIDA_DO_USUARIO && !envolveUsuario) {
            return ResultadoProcessamentoEvento.paraNaoEnvolverUsuario();
        }

        evento.getTemporada().avancarDataAtualPara(evento.getData());

        SimulacaoResponseDTO simulacao = null;
        boolean rodadaConcluida = false;
        if (!partida.isFinalizada()) {
            ResultadoSimulacaoAgendada resultado = simularRodadaUseCase.simularPartidaAgendada(
                    evento.getEdicao().getId(), evento.getPartidaId(), evento.isPartidaMataMata(),
                    modo == ModoProcessamento.PARTIDA_DO_USUARIO, evento.getData());
            simulacao = resultado.simulacao();
            rodadaConcluida = resultado.rodadaConcluida();
        }

        evento.concluir();
        return ResultadoProcessamentoEvento.processado(simulacao, rodadaConcluida);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErro(Long eventoId, String mensagem) {
        EventoCalendarioEntity evento = eventoRepository.findByIdForUpdate(eventoId)
                .orElseThrow(() -> new NaoEncontradoException(
                        "Acontecimento do calendário não encontrado"));
        if (evento.estaAberto()) {
            evento.registrarErro(mensagem);
        }
    }

    private PartidaSimulavel carregarPartida(EventoCalendarioEntity evento) {
        if (evento.isPartidaMataMata()) {
            return partidaMataMataRepository.findById(evento.getPartidaId())
                    .orElseThrow(() -> new NaoEncontradoException("Partida do evento não encontrada"));
        }
        return partidaRepository.findById(evento.getPartidaId())
                .orElseThrow(() -> new NaoEncontradoException("Partida do evento não encontrada"));
    }

    private enum ModoProcessamento {
        AVANCO_AUTOMATICO,
        PARTIDA_DO_USUARIO
    }

    public record ResultadoProcessamentoEvento(
            SimulacaoResponseDTO simulacao,
            boolean aguardandoUsuario,
            boolean ignorado,
            boolean rodadaConcluida,
            boolean naoEnvolveUsuario
    ) {
        private static ResultadoProcessamentoEvento paraAguardarUsuario() {
            return new ResultadoProcessamentoEvento(null, true, false, false, false);
        }

        private static ResultadoProcessamentoEvento paraIgnorado() {
            return new ResultadoProcessamentoEvento(null, false, true, false, false);
        }

        private static ResultadoProcessamentoEvento paraNaoEnvolverUsuario() {
            return new ResultadoProcessamentoEvento(null, false, false, false, true);
        }

        private static ResultadoProcessamentoEvento processado(
                SimulacaoResponseDTO simulacao, boolean rodadaConcluida) {
            return new ResultadoProcessamentoEvento(
                    simulacao, false, false, rodadaConcluida, false);
        }
    }
}
