package com.futmaneger.application.usecase.calendario;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.entity.FaseCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.FormatoFase;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.ReservaCalendarioMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TipoEventoCalendario;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.FaseCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.ReservaCalendarioMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendamentoCalendarioService {

    static final int DIAS_DESCANSO = 2;
    private static final int INTERVALO_MINIMO_RODADAS = DIAS_DESCANSO + 1;
    private static final int INTERVALO_MAXIMO_RODADAS = 7;

    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final EventoCalendarioRepository eventoRepository;
    private final TemporadaJpaRepository temporadaRepository;
    private final ClubeParticipanteRepository participanteRepository;
    private final FaseCampeonatoRepository faseRepository;
    private final ReservaCalendarioMataMataRepository reservaRepository;

    public AgendamentoCalendarioService(PartidaRepository partidaRepository,
                                        PartidaMataMataRepository partidaMataMataRepository,
                                        EventoCalendarioRepository eventoRepository,
                                        TemporadaJpaRepository temporadaRepository,
                                        ClubeParticipanteRepository participanteRepository,
                                        FaseCampeonatoRepository faseRepository,
                                        ReservaCalendarioMataMataRepository reservaRepository) {
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.eventoRepository = eventoRepository;
        this.temporadaRepository = temporadaRepository;
        this.participanteRepository = participanteRepository;
        this.faseRepository = faseRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Gera o calendário completo. As fases futuras de mata-mata recebem reservas
     * antes das ligas, para que a temporada não fique sem datas após ser iniciada.
     */
    @Transactional
    public void gerarCalendario(TemporadaEntity temporada) {
        LocalDate dataBase = temporada.getDataInicio();
        if (dataBase == null) {
            throw new DadosInvalidosException("A temporada não possui data de início para gerar o calendário");
        }
        reservaRepository.deleteAll(reservaRepository.findByEdicao_Temporada(temporada));
        sincronizar(temporada, dataBase);
        for (EdicaoCampeonatoEntity edicao : temporada.getEdicoes()) {
            if (eventoRepository.countByEdicao(edicao) == 0) {
                throw new DadosInvalidosException(
                        "O campeonato '" + edicao.getCampeonato().getNome()
                                + "' não possui partidas geradas. Gere as rodadas antes de iniciar a temporada");
            }
        }
    }

    /** Agenda partidas criadas durante a temporada usando primeiro as datas reservadas. */
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

        Map<Long, List<Long>> participantesPorEdicao = new HashMap<>();
        for (ReservaCalendarioMataMataEntity reserva : reservaRepository.findByEdicao_Temporada(temporada)) {
            List<Long> clubes = participantesPorEdicao.computeIfAbsent(reserva.getEdicao().getId(),
                    id -> idsParticipantes(reserva.getEdicao()));
            ocupar(clubes, reserva.getData(), ocupacao);
        }

        for (Compromisso compromisso : agendadosAbertos) {
            criarEventoSeNecessario(temporada, compromisso, compromisso.dataProgramada());
        }

        edicoes.sort(Comparator
                .comparing((EdicaoCampeonatoEntity e) -> !ehMataMata(e))
                .thenComparing(e -> inicioDaJanela(temporada, e),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(EdicaoCampeonatoEntity::getId));

        boolean calendarioInicial = temporada.getEstado() == TemporadaEntity.EstadoTemporada.AGUARDANDO_INICIO
                && !temporada.isCalendarioGerado();
        int agendadas = 0;
        for (EdicaoCampeonatoEntity edicao : edicoes) {
            List<Compromisso> pendentes = new ArrayList<>(
                    pendentesPorEdicao.getOrDefault(edicao.getId(), List.of()));
            agendadas += pendentes.size();

            if (ehMataMata(edicao) && !pendentes.isEmpty()) {
                agendarNasReservas(temporada, edicao, pendentes, dataMinima);
            }
            if (!pendentes.isEmpty()) {
                planejarEdicao(temporada, edicao, pendentes, dataMinima, ocupacao,
                        ultimaDataPorEdicao.get(edicao.getId()));
            }

            LocalDate ultimaData = compromissosDaEdicao(edicao).stream()
                    .map(Compromisso::dataProgramada)
                    .filter(data -> data != null)
                    .max(LocalDate::compareTo)
                    .orElse(ultimaDataPorEdicao.get(edicao.getId()));
            ultimaDataPorEdicao.put(edicao.getId(), ultimaData);

            if (calendarioInicial && ehMataMata(edicao)) {
                reservarFasesFuturas(temporada, edicao, ultimaData, ocupacao);
            }
        }
        return agendadas;
    }

    private void agendarNasReservas(TemporadaEntity temporada, EdicaoCampeonatoEntity edicao,
                                    List<Compromisso> pendentes, LocalDate dataMinima) {
        List<ReservaCalendarioMataMataEntity> reservas =
                reservaRepository.findByEdicaoOrderByOrdemAsc(edicao);
        for (ReservaCalendarioMataMataEntity reserva : reservas) {
            if (reserva.getData().isBefore(dataMinima)) {
                reservaRepository.delete(reserva);
                continue;
            }
            if (pendentes.isEmpty()) {
                break;
            }
            Set<Long> usadosNaData = new HashSet<>();
            List<Compromisso> atribuidos = new ArrayList<>();
            for (Compromisso compromisso : pendentes) {
                if (usadosNaData.contains(compromisso.mandanteId())
                        || usadosNaData.contains(compromisso.visitanteId())) {
                    continue;
                }
                compromisso.definirData(reserva.getData());
                compromisso.salvar();
                criarEventoSeNecessario(temporada, compromisso, reserva.getData());
                usadosNaData.add(compromisso.mandanteId());
                usadosNaData.add(compromisso.visitanteId());
                atribuidos.add(compromisso);
            }
            if (!atribuidos.isEmpty()) {
                pendentes.removeAll(atribuidos);
                reservaRepository.delete(reserva);
            }
        }
    }

    private void planejarEdicao(TemporadaEntity temporada, EdicaoCampeonatoEntity edicao,
                                List<Compromisso> pendentes, LocalDate dataMinima,
                                Map<Long, TreeSet<LocalDate>> ocupacao, LocalDate ultimaDataDaEdicao) {
        LocalDate inicio = dataMinima;
        LocalDate inicioDaJanela = inicioDaJanela(temporada, edicao);
        if (inicioDaJanela != null) {
            inicio = maisTarde(inicio, inicioDaJanela);
        }
        if (ultimaDataDaEdicao != null) {
            inicio = maisTarde(inicio, ultimaDataDaEdicao.plusDays(1));
        }
        LocalDate fim = fimDaJanela(temporada, edicao);

        TreeMap<Integer, List<Compromisso>> porRodada = agruparPorRodada(pendentes);
        int intervaloIdeal = calcularIntervalo(edicao.getCampeonato(), inicio, fim, porRodada.size());
        Map<Compromisso, LocalDate> plano = null;
        for (int intervalo = intervaloIdeal; intervalo >= INTERVALO_MINIMO_RODADAS; intervalo--) {
            plano = tentarPlanejar(porRodada, inicio, fim, intervalo, ocupacao);
            if (plano != null) {
                break;
            }
        }

        LocalDate fimTemporada = temporada.getDataEncerramento();
        if (plano == null && temporada.getEstado() == TemporadaEntity.EstadoTemporada.EM_ANDAMENTO
                && fimTemporada != null && (fim == null || fim.isBefore(fimTemporada))) {
            plano = tentarPlanejar(porRodada, inicio, fimTemporada,
                    INTERVALO_MINIMO_RODADAS, ocupacao);
        }
        if (plano == null) {
            throw erroCalendarioInviavel(edicao, inicioDaJanela, fim, porRodada, pendentes.get(0));
        }

        for (Map.Entry<Compromisso, LocalDate> item : plano.entrySet()) {
            Compromisso compromisso = item.getKey();
            LocalDate data = item.getValue();
            compromisso.definirData(data);
            compromisso.salvar();
            ocupar(compromisso, data, ocupacao);
            criarEventoSeNecessario(temporada, compromisso, data);
        }
        pendentes.clear();
    }

    private Map<Compromisso, LocalDate> tentarPlanejar(TreeMap<Integer, List<Compromisso>> porRodada,
                                                       LocalDate inicio, LocalDate fim, int intervalo,
                                                       Map<Long, TreeSet<LocalDate>> ocupacaoAtual) {
        Map<Long, TreeSet<LocalDate>> ocupacao = copiarOcupacao(ocupacaoAtual);
        Map<Compromisso, LocalDate> plano = new LinkedHashMap<>();
        LocalDate pisoDaRodada = inicio;
        for (List<Compromisso> rodada : porRodada.values()) {
            LocalDate ultimaDaRodada = pisoDaRodada;
            for (Compromisso compromisso : rodada) {
                LocalDate data = primeiraDataLivre(compromisso, pisoDaRodada, fim, ocupacao);
                if (data == null) {
                    return null;
                }
                plano.put(compromisso, data);
                ocupar(compromisso, data, ocupacao);
                ultimaDaRodada = maisTarde(ultimaDaRodada, data);
            }
            pisoDaRodada = maisTarde(pisoDaRodada.plusDays(intervalo), ultimaDaRodada.plusDays(1));
        }
        return plano;
    }

    private void reservarFasesFuturas(TemporadaEntity temporada, EdicaoCampeonatoEntity edicao,
                                      LocalDate ultimaData, Map<Long, TreeSet<LocalDate>> ocupacao) {
        int quantidade = quantidadeDatasFuturas(edicao);
        if (quantidade == 0) {
            return;
        }
        List<Long> clubes = idsParticipantes(edicao);
        LocalDate inicio = ultimaData == null ? inicioDaJanela(temporada, edicao) : ultimaData.plusDays(1);
        LocalDate fim = fimDaJanela(temporada, edicao);
        LocalDate piso = inicio;
        for (int ordem = 1; ordem <= quantidade; ordem++) {
            LocalDate data = primeiraDataLivre(clubes, piso, fim, ocupacao);
            if (data == null) {
                long dias = inicio != null && fim != null ? ChronoUnit.DAYS.between(inicio, fim) + 1 : 0;
                throw new DadosInvalidosException(
                        "O calendário do campeonato '" + edicao.getCampeonato().getNome()
                                + "' é inviável: foi possível reservar " + (ordem - 1) + " de " + quantidade
                                + " data(s) necessárias para as fases futuras dentro do período "
                                + inicioDaJanela(temporada, edicao) + " a " + fim + " (" + dias + " dias). "
                                + "Amplie o período ou reduza a quantidade de jogos");
            }
            reservaRepository.save(new ReservaCalendarioMataMataEntity(edicao, ordem, data));
            ocupar(clubes, data, ocupacao);
            piso = data.plusDays(1);
        }
    }

    private int quantidadeDatasFuturas(EdicaoCampeonatoEntity edicao) {
        List<FaseCampeonatoEntity> fases = faseRepository
                .findByCampeonatoOrderByOrdem(edicao.getCampeonato());
        if (!fases.isEmpty()) {
            int ordemAtual = partidaMataMataRepository.findByEdicaoId(edicao.getId()).stream()
                    .map(PartidaMataMataEntity::getOrdemFase)
                    .filter(ordem -> ordem != null)
                    .max(Integer::compareTo)
                    .orElse(0);
            return fases.stream()
                    .filter(fase -> fase.getOrdem() > ordemAtual)
                    .mapToInt(fase -> fase.getFormato() == FormatoFase.IDA_E_VOLTA ? 2 : 1)
                    .sum();
        }

        int classificados = participanteRepository.findByEdicao(edicao).size() / 2;
        int fasesFuturas = 0;
        while (classificados > 1) {
            classificados /= 2;
            fasesFuturas++;
        }
        return fasesFuturas;
    }

    private DadosInvalidosException erroCalendarioInviavel(
            EdicaoCampeonatoEntity edicao, LocalDate inicio, LocalDate fim,
            TreeMap<Integer, List<Compromisso>> porRodada, Compromisso semData) {
        long dias = inicio != null && fim != null ? ChronoUnit.DAYS.between(inicio, fim) + 1 : 0;
        int partidas = porRodada.values().stream().mapToInt(List::size).sum();
        return new DadosInvalidosException(
                "Não foi possível agendar o campeonato '" + edicao.getCampeonato().getNome()
                        + "' dentro do período previsto: " + porRodada.size() + " rodada(s) e " + partidas + " partida(s) pendentes em "
                        + dias + " dias (" + inicio + " a " + fim + "), considerando " + DIAS_DESCANSO
                        + " dias completos de descanso e as outras competições. Primeira partida sem data: clube "
                        + semData.mandanteId() + " x clube " + semData.visitanteId()
                        + ". Amplie o período ou reduza a quantidade de jogos");
    }

    private TreeMap<Integer, List<Compromisso>> agruparPorRodada(List<Compromisso> pendentes) {
        TreeMap<Integer, List<Compromisso>> porRodada = new TreeMap<>();
        for (Compromisso compromisso : pendentes) {
            porRodada.computeIfAbsent(compromisso.ordem(), k -> new ArrayList<>()).add(compromisso);
        }
        return porRodada;
    }

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
                                        Map<Long, TreeSet<LocalDate>> ocupacao) {
        for (LocalDate data = piso; limite == null || !data.isAfter(limite); data = data.plusDays(1)) {
            if (clubeLivre(compromisso.mandanteId(), data, ocupacao)
                    && clubeLivre(compromisso.visitanteId(), data, ocupacao)) {
                return data;
            }
        }
        return null;
    }

    private LocalDate primeiraDataLivre(List<Long> clubes, LocalDate piso, LocalDate limite,
                                        Map<Long, TreeSet<LocalDate>> ocupacao) {
        for (LocalDate data = piso; limite == null || !data.isAfter(limite); data = data.plusDays(1)) {
            boolean todosLivres = true;
            for (Long clube : clubes) {
                if (!clubeLivre(clube, data, ocupacao)) {
                    todosLivres = false;
                    break;
                }
            }
            if (todosLivres) {
                return data;
            }
        }
        return null;
    }

    private boolean clubeLivre(Long clubeId, LocalDate data, Map<Long, TreeSet<LocalDate>> ocupacao) {
        TreeSet<LocalDate> ocupadas = ocupacao.get(clubeId);
        if (ocupadas == null) {
            return true;
        }
        return ocupadas.subSet(data.minusDays(DIAS_DESCANSO), true,
                data.plusDays(DIAS_DESCANSO), true).isEmpty();
    }

    private void ocupar(Compromisso compromisso, LocalDate data,
                        Map<Long, TreeSet<LocalDate>> ocupacao) {
        ocupacao.computeIfAbsent(compromisso.mandanteId(), k -> new TreeSet<>()).add(data);
        ocupacao.computeIfAbsent(compromisso.visitanteId(), k -> new TreeSet<>()).add(data);
    }

    private void ocupar(List<Long> clubes, LocalDate data,
                        Map<Long, TreeSet<LocalDate>> ocupacao) {
        clubes.forEach(clube -> ocupacao.computeIfAbsent(clube, k -> new TreeSet<>()).add(data));
    }

    private Map<Long, TreeSet<LocalDate>> copiarOcupacao(Map<Long, TreeSet<LocalDate>> origem) {
        Map<Long, TreeSet<LocalDate>> copia = new HashMap<>();
        origem.forEach((clube, datas) -> copia.put(clube, new TreeSet<>(datas)));
        return copia;
    }

    private List<Long> idsParticipantes(EdicaoCampeonatoEntity edicao) {
        return participanteRepository.findByEdicao(edicao).stream()
                .map(ClubeParticipanteEntity::getClube)
                .map(clube -> clube.getId())
                .toList();
    }

    private boolean ehMataMata(EdicaoCampeonatoEntity edicao) {
        return edicao.getCampeonato().getTipo() == CampeonatoEntity.TipoCampeonato.MATA_MATA
                || !faseRepository.findByCampeonatoOrderByOrdem(edicao.getCampeonato()).isEmpty();
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

        CompromissoPontosCorridos(EdicaoCampeonatoEntity edicao, PartidaEntity partida,
                                  PartidaRepository repository) {
            this.edicao = edicao;
            this.partida = partida;
            this.repository = repository;
        }

        @Override public EdicaoCampeonatoEntity edicao() { return edicao; }
        @Override public Long partidaId() { return partida.getId(); }
        @Override public boolean mataMata() { return false; }
        @Override public Long mandanteId() { return partida.getMandante().getId(); }
        @Override public Long visitanteId() { return partida.getVisitante().getId(); }
        @Override public int ordem() { return partida.getRodada(); }
        @Override public boolean finalizada() { return partida.isFinalizada(); }
        @Override public LocalDate dataProgramada() { return partida.getDataProgramada(); }
        @Override public void definirData(LocalDate data) { partida.setDataProgramada(data); }
        @Override public void salvar() { repository.save(partida); }
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

        @Override public EdicaoCampeonatoEntity edicao() { return edicao; }
        @Override public Long partidaId() { return partida.getId(); }
        @Override public boolean mataMata() { return true; }
        @Override public Long mandanteId() { return partida.getMandante().getId(); }
        @Override public Long visitanteId() { return partida.getVisitante().getId(); }
        @Override public boolean finalizada() { return partida.isFinalizada(); }
        @Override public LocalDate dataProgramada() { return partida.getDataProgramada(); }
        @Override public void definirData(LocalDate data) { partida.setDataProgramada(data); }
        @Override public void salvar() { repository.save(partida); }

        @Override
        public int ordem() {
            if (partida.getRodada() != null) {
                return partida.getRodada().getNumero();
            }
            return partida.getOrdemFase() != null ? partida.getOrdemFase() : 0;
        }
    }
}
