package com.futmaneger.application.usecase.calendario;

import com.futmaneger.application.dto.AvancoTemporadaResponseDTO;
import com.futmaneger.application.dto.ResultadoPartidaResumoDTO;
import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.application.dto.SituacaoAvanco;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.config.CalendarioProperties;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.entity.StatusEventoCalendario;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Conduz a temporada até o próximo acontecimento relevante: simula
 * automaticamente as partidas dos demais clubes e para quando chega uma partida
 * do clube do usuário, quando atinge o limite da chamada ou quando não há mais
 * pendências.
 *
 * <p>Não é transacional de propósito: cada acontecimento é processado em sua
 * própria transação, de modo que uma falha não desfaz o que já foi concluído e o
 * avanço pode ser retomado sem repetir partidas.</p>
 */
@Service
public class AvancarTemporadaUseCase {

    private final TemporadaJpaRepository temporadaRepository;
    private final EventoCalendarioRepository eventoRepository;
    private final AgendamentoCalendarioService agendamentoCalendario;
    private final ProcessadorEventoCalendarioService processador;
    private final ConsultarCalendarioUseCase consultarCalendario;
    private final ClubeDoUsuarioService clubeDoUsuarioService;
    private final CalendarioProperties calendarioProperties;

    public AvancarTemporadaUseCase(TemporadaJpaRepository temporadaRepository,
                                   EventoCalendarioRepository eventoRepository,
                                   AgendamentoCalendarioService agendamentoCalendario,
                                   ProcessadorEventoCalendarioService processador,
                                   ConsultarCalendarioUseCase consultarCalendario,
                                   ClubeDoUsuarioService clubeDoUsuarioService,
                                   CalendarioProperties calendarioProperties) {
        this.temporadaRepository = temporadaRepository;
        this.eventoRepository = eventoRepository;
        this.agendamentoCalendario = agendamentoCalendario;
        this.processador = processador;
        this.consultarCalendario = consultarCalendario;
        this.clubeDoUsuarioService = clubeDoUsuarioService;
        this.calendarioProperties = calendarioProperties;
    }

    public AvancoTemporadaResponseDTO executar(Long temporadaId) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));

        if (temporada.getEstado() != TemporadaEntity.EstadoTemporada.EM_ANDAMENTO) {
            throw new DadosInvalidosException(
                    "Somente temporadas em andamento podem avançar no calendário");
        }
        if (!temporada.isCalendarioGerado()) {
            throw new DadosInvalidosException("O calendário desta temporada ainda não foi gerado");
        }

        ClubeEntity clubeUsuario = clubeDoUsuarioService.obterClubeDoUsuarioAutenticado()
                .orElseThrow(() -> new DadosInvalidosException(
                        "O técnico autenticado não está vinculado a nenhum clube. "
                                + "Vincule-o a um clube para avançar a temporada"));

        List<ResultadoPartidaResumoDTO> resultados = new ArrayList<>();

        try {
            agendamentoCalendario.sincronizarSeNecessario(temporadaId);
        } catch (RuntimeException e) {
            return resposta(temporadaId, SituacaoAvanco.ERRO, resultados,
                    mensagemDe(e), clubeUsuario.getId());
        }

        int eventosProcessados = 0;
        while (true) {
            Optional<EventoCalendarioEntity> proximo = eventoRepository
                    .findFirstByTemporadaAndStatusInOrderByDataAscOrdemAscIdAsc(
                            temporada, StatusEventoCalendario.abertos());

            if (proximo.isEmpty()) {
                return resposta(temporadaId, SituacaoAvanco.SEM_PENDENCIAS, resultados,
                        "Todos os acontecimentos da temporada foram concluídos", clubeUsuario.getId());
            }

            EventoCalendarioEntity evento = proximo.get();
            ProcessadorEventoCalendarioService.ResultadoProcessamentoEvento processamento;
            try {
                processamento = processador.processarParaAvanco(
                        evento.getId(), clubeUsuario.getId());
            } catch (RuntimeException e) {
                processador.registrarErro(evento.getId(), mensagemDe(e));
                return resposta(temporadaId, SituacaoAvanco.ERRO, resultados,
                        "Falha ao processar o acontecimento " + evento.getId() + ": " + mensagemDe(e),
                        clubeUsuario.getId());
            }

            if (processamento.ignorado()) {
                continue;
            }
            if (processamento.aguardandoUsuario()) {
                return resposta(temporadaId, SituacaoAvanco.AGUARDANDO_USUARIO, resultados,
                        "Partida do seu clube aguardando para ser disputada em " + evento.getData(),
                        clubeUsuario.getId());
            }

            SimulacaoResponseDTO simulacao = processamento.simulacao();
            if (simulacao != null) {
                resultados.add(new ResultadoPartidaResumoDTO(
                        evento.getId(),
                        evento.getData(),
                        evento.getEdicao().getCampeonato().getNome(),
                        simulacao.nomeMandante(),
                        simulacao.golsMandante(),
                        simulacao.nomeVisitante(),
                        simulacao.golsVisitante()));
            }
            eventosProcessados++;

            if (processamento.rodadaConcluida()) {
                try {
                    agendamentoCalendario.sincronizarSeNecessario(temporadaId);
                } catch (RuntimeException e) {
                    return resposta(temporadaId, SituacaoAvanco.ERRO, resultados,
                            "Falha ao agendar as próximas partidas: " + mensagemDe(e),
                            clubeUsuario.getId());
                }
            }

            if (eventosProcessados >= calendarioProperties.getMaxEventosPorAvanco()) {
                return resposta(temporadaId, SituacaoAvanco.LIMITE_ATINGIDO, resultados,
                        "O limite de " + calendarioProperties.getMaxEventosPorAvanco()
                                + " acontecimentos por chamada foi atingido; continue o avanço",
                        clubeUsuario.getId());
            }
        }
    }

    private AvancoTemporadaResponseDTO resposta(Long temporadaId, SituacaoAvanco situacao,
                                                List<ResultadoPartidaResumoDTO> resultados,
                                                String mensagem, Long clubeUsuarioId) {
        ConsultarCalendarioUseCase.ResumoAvanco resumo =
                consultarCalendario.resumoParaAvanco(temporadaId, clubeUsuarioId);

        return new AvancoTemporadaResponseDTO(
                temporadaId,
                resumo.dataAtual(),
                situacao,
                resultados.size(),
                resultados,
                resumo.proximoEvento(),
                resumo.podeFinalizar(),
                mensagem
        );
    }

    private String mensagemDe(RuntimeException e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
