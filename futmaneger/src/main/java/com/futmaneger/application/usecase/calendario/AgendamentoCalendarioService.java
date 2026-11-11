package com.futmaneger.application.usecase.calendario;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Distribui as datas das partidas de todas as edições de uma temporada em um
 * planejamento global. Cada partida agendada gera um evento no calendário.
 *
 * <p>Regras principais:</p>
 * <ul>
 *   <li>cada edição só joga dentro da sua janela prevista (ou da temporada, se o
 *       campeonato não define período);</li>
 *   <li>um clube tem sempre {@value #DIAS_DESCANSO} dias completos de descanso entre
 *       jogos, mesmo entre competições diferentes;</li>
 *   <li>as rodadas de uma edição acontecem em ordem; em pontos corridos elas são
 *       espalhadas pela janela, deixando datas livres para outras competições;</li>
 *   <li>a sincronização é incremental: considera as datas já ocupadas e agenda
 *       apenas as partidas sem data, o que permite agendar as fases seguintes de
 *       mata-mata quando forem geradas.</li>
 * </ul>
 */
@Service
public class AgendamentoCalendarioService {

    static final int DIAS_DESCANSO = 2;
    private static final int INTERVALO_MINIMO_RODADAS = DIAS_DESCANSO + 1;
    private static final int INTERVALO_MAXIMO_RODADAS = 7;

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
     * alguma partida não couber no período previsto.
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
        Map<Long, TreeSet<LocalDate>> ocupacao = new HashMap<>();
        Map<Long, LocalDate> ultimaDataPorEdicao = new HashMap<>();
        Map<Long, List<Compromisso>> pendentesPorEdicao = new LinkedHashMap<>();
        List<Compromisso> agendadosAbertos = new ArrayList<>();

        List<EdicaoCampeonatoEntity> edicoes = new ArrayList<>(temporada.getEdicoes());

        for (EdicaoCampeonatoEntity edicao : edicoes) {
            for (Compromisso compromisso : compromissosDaEdicao(edicao)) {
                if (compromisso.dataProgramada() != null) {
                    ocupar(compromisso, compromisso.dataProgramada(), ocupacao);
                    ultimaDataPorEdicao.merge(edicao.getId(), compromisso.dataProgramada(), this::maisTarde);
                    if (!compromisso.finalizada()) {
                        agendadosAbertos.add(compromisso);
                    }
                } else if (!compromisso.finalizada()) {
                    pendentesPorEdicao.computeIfAbsent(edicao.getId(), k -> new ArrayList<>()).add(compromisso);
                }
            }
        }

        for (Compromisso compromisso : agendadosAbertos) {
            criarEventoSeNecessario(temporada, compromisso, compromisso.dataProgramada());
        }

        edicoes.sort(Comparator
                .comparing((EdicaoCampeonatoEntity e) -> inicioDaJanela(temporada, e),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(EdicaoCampeonatoEntity::getId));

        int agendadas = 0;
        for (EdicaoCampeonatoEntity edicao : edicoes) {
            List<Compromisso> pendentes = pendentesPorEdicao.get(edicao.getId());
            if (pendentes == null || pendentes.isEmpty()) {
                continue;
            }
            agendarEdicao(temporada, edicao, pendentes, dataMinima, ocupacao,
                    ultimaDataPorEdicao.get(edicao.getId()));
            agendadas += pendentes.size();
        }

        return agendadas;
    }

    private void agendarEdicao(TemporadaEntity temporada, EdicaoCampeonatoEntity edicao,
                               List<Compromisso> pendentes, LocalDate dataMinima,
                               Map<Long, TreeSet<LocalDate>> ocupacao, LocalDate ultimaDataDaEdicao) {
        LocalDate fim = fimDaJanela(temporada, edicao);

        LocalDate inicio = dataMinima;
        LocalDate inicioDaJanela = inicioDaJanela(temporada, edicao);
        if (inicioDaJanela != null) {
            inicio = maisTarde(inicio, inicioDaJanela);
        }
        if (ultimaDataDaEdicao != null) {
            inicio = maisTarde(inicio, ultimaDataDaEdicao.plusDays(1));
        }

        TreeMap<Integer, List<Compromisso>> porRodada = new TreeMap<>();
        for (Compromisso compromisso : pendentes) {
            porRodada.computeIfAbsent(compromisso.ordem(), k -> new ArrayList<>()).add(compromisso);
        }

        int intervalo = calcularIntervalo(edicao.getCampeonato(), inicio, fim, porRodada.size());
        LocalDate pisoDaRodada = inicio;

        for (List<Compromisso> rodada : porRodada.values()) {
            LocalDate ultimaDaRodada = pisoDaRodada;

            for (Compromisso compromisso : rodada) {
                LocalDate data = primeiraDataLivre(compromisso, pisoDaRodada, fim, ocupacao, edicao, inicioDaJanela);
                compromisso.definirData(data);
                compromisso.salvar();
                ocupar(compromisso, data, ocupacao);
                criarEventoSeNecessario(temporada, compromisso, data);
                ultimaDaRodada = maisTarde(ultimaDaRodada, data);
            }

            pisoDaRodada = maisTarde(pisoDaRodada.plusDays(intervalo), ultimaDaRodada.plusDays(1));
        }
    }

    /**
     * Em pontos corridos as rodadas são espalhadas pela janela (no máximo uma por
     * semana), deixando datas livres para outras competições dos mesmos clubes.
     * Em mata-mata as fases seguem o mais cedo possível, pois dependem do resultado
     * das anteriores.
     */
    private int calcularIntervalo(CampeonatoEntity campeonato, LocalDate inicio, LocalDate fim, int rodadas) {
        if (campeonato.getTipo() != CampeonatoEntity.TipoCampeonato.PONTOS_CORRIDOS || rodadas <= 1) {
            return INTERVALO_MINIMO_RODADAS;
        }
        if (fim == null) {
            return INTERVALO_MAXIMO_RODADAS;
        }
        long ideal = ChronoUnit.DAYS.between(inicio, fim) / rodadas;
        return (int) Math.max(INTERVALO_MINIMO_RODADAS, Math.min(INTERVALO_MAXIMO_RODADAS, ideal));
    }

    private LocalDate primeiraDataLivre(Compromisso compromisso, LocalDate piso, LocalDate limite,
                                        Map<Long, TreeSet<LocalDate>> ocupacao,
                                        EdicaoCampeonatoEntity edicao, LocalDate inicioDaJanela) {
        for (LocalDate data = piso; limite == null || !data.isAfter(limite); data = data.plusDays(1)) {
            if (clubeLivre(compromisso.mandanteId(), data, ocupacao)
                    && clubeLivre(compromisso.visitanteId(), data, ocupacao)) {
                return data;
            }
        }

        throw new DadosInvalidosException(
                "Não foi possível agendar todas as partidas do campeonato '"
                        + edicao.getCampeonato().getNome() + "' dentro do período previsto ("
                        + (inicioDaJanela != null ? inicioDaJanela : piso) + " a " + limite
                        + "). Partida sem data: clube " + compromisso.mandanteId()
                        + " x clube " + compromisso.visitanteId());
    }

    private boolean clubeLivre(Long clubeId, LocalDate data, Map<Long, TreeSet<LocalDate>> ocupacao) {
        TreeSet<LocalDate> ocupadas = ocupacao.get(clubeId);
        if (ocupadas == null) {
            return true;
        }
        return ocupadas.subSet(data.minusDays(DIAS_DESCANSO), true, data.plusDays(DIAS_DESCANSO), true).isEmpty();
    }

    private void ocupar(Compromisso compromisso, LocalDate data, Map<Long, TreeSet<LocalDate>> ocupacao) {
        ocupacao.computeIfAbsent(compromisso.mandanteId(), k -> new TreeSet<>()).add(data);
        ocupacao.computeIfAbsent(compromisso.visitanteId(), k -> new TreeSet<>()).add(data);
    }

    private LocalDate inicioDaJanela(TemporadaEntity temporada, EdicaoCampeonatoEntity edicao) {
        return edicao.getDataInicioPrevista() != null ? edicao.getDataInicioPrevista() : temporada.getDataInicio();
    }

    private LocalDate fimDaJanela(TemporadaEntity temporada, EdicaoCampeonatoEntity edicao) {
        LocalDate fimDaEdicao = edicao.getDataFimPrevista();
        LocalDate fimDaTemporada = temporada.getDataEncerramento();
        if (fimDaEdicao == null) {
            return fimDaTemporada;
        }
        if (fimDaTemporada == null) {
            return fimDaEdicao;
        }
        return fimDaEdicao.isBefore(fimDaTemporada) ? fimDaEdicao : fimDaTemporada;
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

    private LocalDate maisTarde(LocalDate a, LocalDate b) {
        if (b == null) {
            return a;
        }
        return b.isAfter(a) ? b : a;
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
