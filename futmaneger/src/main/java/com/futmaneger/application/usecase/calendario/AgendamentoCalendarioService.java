package com.futmaneger.application.usecase.calendario;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TipoEventoCalendario;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Distribui as datas das partidas de todas as edições de uma temporada em um
 * planejamento global, respeitando o descanso mínimo entre jogos de um mesmo
 * clube e evitando compromissos incompatíveis. Cada partida agendada gera um
 * evento no calendário.
 *
 * <p>A sincronização é incremental: considera as datas já ocupadas e agenda
 * apenas as partidas ainda sem data. Assim, as fases seguintes de mata-mata,
 * que só existem depois de conhecidos os classificados, são agendadas
 * dinamicamente quando geradas.</p>
 */
@Service
public class AgendamentoCalendarioService {

    private static final int DIAS_DESCANSO = 2;

    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final EventoCalendarioRepository eventoRepository;
    private final TemporadaJpaRepository temporadaRepository;

    public AgendamentoCalendarioService(PartidaRepository partidaRepository,
                                        PartidaMataMataRepository partidaMataMataRepository,
                                        EventoCalendarioRepository eventoRepository,
                                        TemporadaJpaRepository temporadaRepository) {
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.eventoRepository = eventoRepository;
        this.temporadaRepository = temporadaRepository;
    }

    /**
     * Gera o calendário inicial. Falha se alguma edição não tiver partidas ou se
     * alguma partida não couber no período da temporada.
     */
    @Transactional
    public void gerarCalendario(TemporadaEntity temporada) {
        LocalDate dataBase = temporada.getDataInicio();
        if (dataBase == null) {
            throw new DadosInvalidosException("A temporada não possui data de início para gerar o calendário");
        }

        sincronizar(temporada, dataBase);

        for (EdicaoCampeonatoEntity edicao : temporada.getEdicoes()) {
            if (eventoRepository.countByEdicao(edicao) == 0) {
                throw new DadosInvalidosException(
                        "O campeonato '" + edicao.getCampeonato().getNome()
                                + "' não possui partidas geradas. Gere as rodadas antes de iniciar a temporada");
            }
        }
    }

    /**
     * Agenda as partidas que ainda não possuem data (por exemplo, as geradas
     * após o encerramento de uma fase de mata-mata), a partir da data atual.
     */
    @Transactional
    public int sincronizarPorId(Long temporadaId) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));
        LocalDate dataMinima = temporada.getDataAtual() != null ? temporada.getDataAtual() : temporada.getDataInicio();
        return sincronizar(temporada, dataMinima);
    }

    private int sincronizar(TemporadaEntity temporada, LocalDate dataMinima) {
        LocalDate limite = temporada.getDataEncerramento();
        Map<Long, LocalDate> ultimaDataPorClube = new HashMap<>();
        List<Compromisso> semData = new ArrayList<>();
        List<Compromisso> agendadosAbertos = new ArrayList<>();

        for (EdicaoCampeonatoEntity edicao : temporada.getEdicoes()) {
            for (Compromisso compromisso : compromissosDaEdicao(edicao)) {
                if (compromisso.dataProgramada() != null) {
                    registrarOcupacao(compromisso, compromisso.dataProgramada(), ultimaDataPorClube);
                    if (!compromisso.finalizada()) {
                        agendadosAbertos.add(compromisso);
                    }
                } else if (!compromisso.finalizada()) {
                    semData.add(compromisso);
                }
            }
        }

        for (Compromisso compromisso : agendadosAbertos) {
            criarEventoSeNecessario(temporada, compromisso, compromisso.dataProgramada());
        }

        semData.sort(Comparator.comparingInt(Compromisso::ordem));

        for (Compromisso compromisso : semData) {
            LocalDate data = encontrarDataDisponivel(compromisso, ultimaDataPorClube, dataMinima, limite);
            compromisso.definirData(data);
            compromisso.salvar();
            registrarOcupacao(compromisso, data, ultimaDataPorClube);
            criarEventoSeNecessario(temporada, compromisso, data);
        }

        return semData.size();
    }

    private List<Compromisso> compromissosDaEdicao(EdicaoCampeonatoEntity edicao) {
        List<Compromisso> compromissos = new ArrayList<>();
        for (PartidaEntity partida : partidaRepository.findByEdicao(edicao)) {
            compromissos.add(new CompromissoPontosCorridos(edicao, partida, partidaRepository));
        }
        for (PartidaMataMataEntity partida : partidaMataMataRepository.findByEdicaoId(edicao.getId())) {
            compromissos.add(new CompromissoMataMata(edicao, partida, partidaMataMataRepository));
        }
        return compromissos;
    }

    private void criarEventoSeNecessario(TemporadaEntity temporada, Compromisso compromisso, LocalDate data) {
        if (eventoRepository.existsByTipoAndPartidaIdAndPartidaMataMata(
                TipoEventoCalendario.PARTIDA, compromisso.partidaId(), compromisso.mataMata())) {
            return;
        }
        eventoRepository.save(EventoCalendarioEntity.paraPartida(
                temporada, compromisso.edicao(), compromisso.partidaId(), compromisso.mataMata(),
                data, compromisso.ordem()));
    }

    private LocalDate encontrarDataDisponivel(Compromisso compromisso,
                                              Map<Long, LocalDate> ultimaDataPorClube,
                                              LocalDate dataMinima, LocalDate limite) {
        Long mandante = compromisso.mandanteId();
        Long visitante = compromisso.visitanteId();

        LocalDate candidata = dataMinima;
        candidata = maisTarde(candidata, proximaDataLivre(ultimaDataPorClube.get(mandante)));
        candidata = maisTarde(candidata, proximaDataLivre(ultimaDataPorClube.get(visitante)));

        if (limite != null && candidata.isAfter(limite)) {
            throw new DadosInvalidosException(
                    "Não foi possível agendar todas as partidas dentro do período da temporada. "
                            + "Partida sem data: clube " + mandante + " x clube " + visitante);
        }

        return candidata;
    }

    private LocalDate proximaDataLivre(LocalDate ultimaData) {
        if (ultimaData == null) {
            return null;
        }
        return ultimaData.plusDays(DIAS_DESCANSO + 1L);
    }

    private LocalDate maisTarde(LocalDate a, LocalDate b) {
        if (b == null) {
            return a;
        }
        return b.isAfter(a) ? b : a;
    }

    private void registrarOcupacao(Compromisso compromisso, LocalDate data,
                                   Map<Long, LocalDate> ultimaDataPorClube) {
        ultimaDataPorClube.merge(compromisso.mandanteId(), data, this::maisTarde);
        ultimaDataPorClube.merge(compromisso.visitanteId(), data, this::maisTarde);
    }

    private interface Compromisso {
        EdicaoCampeonatoEntity edicao();

        Long partidaId();

        boolean mataMata();

        Long mandanteId();

        Long visitanteId();

        int ordem();

        boolean finalizada();

        LocalDate dataProgramada();

        void definirData(LocalDate data);

        void salvar();
    }

    private static final class CompromissoPontosCorridos implements Compromisso {
        private final EdicaoCampeonatoEntity edicao;
        private final PartidaEntity partida;
        private final PartidaRepository repository;

        CompromissoPontosCorridos(EdicaoCampeonatoEntity edicao, PartidaEntity partida, PartidaRepository repository) {
            this.edicao = edicao;
            this.partida = partida;
            this.repository = repository;
        }

        @Override
        public EdicaoCampeonatoEntity edicao() {
            return edicao;
        }

        @Override
        public Long partidaId() {
            return partida.getId();
        }

        @Override
        public boolean mataMata() {
            return false;
        }

        @Override
        public Long mandanteId() {
            return partida.getMandante().getId();
        }

        @Override
        public Long visitanteId() {
            return partida.getVisitante().getId();
        }

        @Override
        public int ordem() {
            return partida.getRodada();
        }

        @Override
        public boolean finalizada() {
            return partida.isFinalizada();
        }

        @Override
        public LocalDate dataProgramada() {
            return partida.getDataProgramada();
        }

        @Override
        public void definirData(LocalDate data) {
            partida.setDataProgramada(data);
        }

        @Override
        public void salvar() {
            repository.save(partida);
        }
    }

    private static final class CompromissoMataMata implements Compromisso {
        private final EdicaoCampeonatoEntity edicao;
        private final PartidaMataMataEntity partida;
        private final PartidaMataMataRepository repository;

        CompromissoMataMata(EdicaoCampeonatoEntity edicao, PartidaMataMataEntity partida,
                            PartidaMataMataRepository repository) {
            this.edicao = edicao;
            this.partida = partida;
            this.repository = repository;
        }

        @Override
        public EdicaoCampeonatoEntity edicao() {
            return edicao;
        }

        @Override
        public Long partidaId() {
            return partida.getId();
        }

        @Override
        public boolean mataMata() {
            return true;
        }

        @Override
        public Long mandanteId() {
            return partida.getMandante().getId();
        }

        @Override
        public Long visitanteId() {
            return partida.getVisitante().getId();
        }

        @Override
        public int ordem() {
            if (partida.getRodada() != null) {
                return partida.getRodada().getNumero();
            }
            return partida.getOrdemFase() != null ? partida.getOrdemFase() : 0;
        }

        @Override
        public boolean finalizada() {
            return partida.isFinalizada();
        }

        @Override
        public LocalDate dataProgramada() {
            return partida.getDataProgramada();
        }

        @Override
        public void definirData(LocalDate data) {
            partida.setDataProgramada(data);
        }

        @Override
        public void salvar() {
            repository.save(partida);
        }
    }
}
